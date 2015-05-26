#!/usr/bin/env python

from multiprocessing import cpu_count
from threading import Lock, Thread
from random import choice
from time import sleep
from urllib import urlopen

from dispatch import timer


dictionary = []
for line in open('dictionary.txt'):
    dictionary.append(line.strip())


class Letter:
    def __init__(self, letter):
        self.letter = letter
        self.known = False


    def __str__(self):
        if self.known:
            return self.letter
        else:
            return "_"


    def guess(self, letter):
        if self.letter is letter:
            self.known = True


    def reveal(self):
        return self.letter


    def blanks(self):
        if self.known:
            return 0
        else:
            return 1


class Word:
    def __init__(self, word=None):
        if word is None: word = choice(dictionary)
        self.success = False
        self.gameover = False
        self.attempts = 0
        self.secret = [Letter(l) for l in word]

        
    def __str__(self):
        return "".join(map(str, self.secret))


    def guess(self, letter):
        if self.gameover:
            return False

        blanks_before = self.blanks()

        for l in self.secret:
            l.guess(letter)

        blanks_after = self.blanks()

        if blanks_after is 0:
            self.success = True
            self.gameover = True

        elif blanks_before is blanks_after:
            self.attempts += 1

            if self.attempts >= 10:
                self.gameover = True

        return blanks_before - blanks_after


    def reveal(self):
        return "".join(map(str, map(Letter.reveal, self.secret)))


    def blanks(self):
        return sum([l.blanks() for l in self.secret])


    def playing(self):
        return not self.gameover


    def won(self):
        return self.success
    


class Einstein:
    def __init__(self):
        self.wordlist = dictionary
        self.guesses = "abcdefghijklmnopqrstuvwxyz"
        self.last_guess = None

    def next(self):
        t = timer();
        t.next()
        if len(self.guesses) is 0: return None
        results = {}
        for letter in self.guesses:
            results[letter] = 0
            for word in self.wordlist:
                if letter in word:
                    results[letter] += 1

        self.last_guess = [k for k,v in sorted(results.iteritems(), key=lambda x: x[1], reverse=True)][0]
        self.guesses = self.guesses.replace(self.last_guess, "")

        #print "guess: %s (%ss)" % (self.last_guess, t.next())
        return self.last_guess

    def set_word_length(self, length):
        new_wordlist = []

        for word in self.wordlist:
            if len(word) is length:
                new_wordlist.append(word)

        self.wordlist = new_wordlist

    def result(self, blanks_filled, pre_result):
        t = timer()
        t.next()
        excluded = 0
        
        new_wordlist = []
        
        if blanks_filled > 0:
            for word in self.wordlist:
                if self.last_guess in word:
                    new_wordlist.append(word)
                    excluded += 1

        else:
            for word in self.wordlist:
                if not self.last_guess in word:
                    new_wordlist.append(word)
                    excluded += 1
                        
        self.wordlist = new_wordlist

        #print "excluded words: %s (%ss)" % (excluded, t.next())
            


class Trial:
    def __init__(self, words, algorithm):
        self.words = words
        self.algorithm = algorithm
        self.run()

    def run(self):
        wins = 0
        for word in self.words:
            algo = self.algorithm()
            while word.playing():
                algo.result(word.guess(algo.next()))

            if word.won():
                wins += 1

            print "%s: %s" % (word.reveal(), word.won())



def word_gen():
    lock = Lock()
    for word in dictionary:
        with lock:
            yield Word(word)



threads = []

#if __name__ == "__main__":
#    wg = word_gen()
#    for i in range(cpu_count()):
#        t = Thread(target=Trial, args=(wg, Einstein))
#        threads.append(t)
#        t.start()
#
#
#    for t in threads:
#        t.join()
#    #t = Trial(wg, Einstein)

#guess = Einstein()

wins = 0

t = timer()
t.next()
tests = 0

wins_by_length = {}
tests_by_length = {}

results = {}

for word in dictionary:
#for i in range(100):
    word = Word(word)
    #word = Word()
    word_length = len(word.reveal())
    if not results.has_key(word_length):
        results[word_length] = {"wins" : 0, "tests" : 0}

    results[word_length]["tests"] += 1
    
    tests += 1
    guess = Einstein()
    guess.set_word_length(word_length)

    while word.playing():
        guess.result(word.guess(guess.next()), str(word))


    if word.won():
        wins += 1
        results[word_length]["wins"] += 1
        print "I won! :) (%s)       | %s" % (word.reveal(), 100*wins/float(tests))
    else:
        print "Word was %s          | %s" % (word.reveal(), 100*wins/float(tests))


print "wins: %s (%s)" % (wins, t.next())

print "results:"
for k, v in results.iteritems():
    print "word length: %s, tests: %s, wins: %s, winrate: %s" % (k, v["tests"], v["wins"], 100*v["wins"]/float(v["tests"]))
