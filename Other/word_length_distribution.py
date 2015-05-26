dictionary = []
for line in open('dictionary.txt'):
    dictionary.append(line.strip())

counts = {}

for word in dictionary:
    unique = len(set(word))
    if counts.has_key(unique):
        counts[unique] += 1
    else:
        counts[unique] = 0

total = len(dictionary)
s = 0


for k,v in counts.iteritems():
    s += v
    print "%s different letters: %s (%s)" % (k, v, 100*s/float(total))
