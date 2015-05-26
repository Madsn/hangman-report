#!/usr/bin/env python

from random import choice, shuffle, triangular
from threading import Lock, Thread
from flask import Flask
from decorator import decorator
from time import time, sleep
from collections import deque
import json

from tornado.wsgi import WSGIContainer
from tornado.httpserver import HTTPServer
from tornado.ioloop import IOLoop

dna_id = 0
dna_id_lock = Lock()

sync_buffer = deque([])
sync_buffer_lock = Lock()

with open("results.txt", 'a') as f:
    f.write("Test started at %s:\n" % time())

def dna_link(dna, extra=""):
    return "<a href=\"%s\">%s</a>" % ("/dna/"+dna, dna)


def next_id():
    with dna_id_lock:
        global dna_id
        id = dna_id
        dna_id +=1
        return id


def timer():
    genesis = time()
    prev = genesis

    while True:
        now = time()
        diff = now - prev
        genesisdiff = now - genesis
        prev = now
        yield "time: %s (%s total)" % (diff, genesisdiff)





dictionary = []

generation_size = 100
buffer_size = 100
buffer_filler_count = 2
job_buffer = deque([])
job_buffer_lock = Lock()
results = {} # {"dna": result, ..}
results_lock = Lock()

start_time = time()

## logic

def get_results():
    return get_results_by_winrate()


def get_results_by_guesses():
    r = []
    for k,v in sorted(results.iteritems(), key=lambda x: x[1]["guesses"]):
        if v["guesses"] is not None:
            r.append(v)
    
    return r

def get_results_by_wrong():
    r = []
    for k,v in sorted(results.iteritems(), key=lambda x: x[1]["wrong"]):
        if v["guesses"] is not None:
            r.append(v)
    
    return r

def get_results_by_winrate():
    r = []
    for k,v in sorted(results.iteritems(), key=lambda x: x[1]["winrate"], reverse=True):
        if v["guesses"] is not None:
            r.append(v)
    
    return r
    

def get_scores(r):
    output = "<table><tr><th>DNA</th><th><a href=\"/wrong\">wrong</a></th><th><a href=\"/winrate\">winrate</a></th><th><a href=\"/guesses\">Guesses</a></th><th>Parents</th><th>id</th></tr>"

    for result in r:
        output += "<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>\n" % (dna_link(result["dna"]),
                                                                                                     result["wrong"],
                                                                                                     result["winrate"],
                                                                                                     result["guesses"],
                                                                                                     result["parents"],
                                                                                                     result["id"])

    return output + "</table>"




random_specimen = 0
random_specimen_lock = Lock()
mated_specimen = 0
mated_specimen_lock = Lock()


def specimen_generator():
    global random_specimen
    global mated_specimen
    
    while True:
        r = get_results()
        result_count = len(r)
        if result_count < 10 or  mated_specimen > 10*random_specimen:
            dna = map(lambda x: x+97, range(26))
            dna = map(chr, dna)

            shuffle(dna)

            dna = "".join(dna)

            #print "random: %s" % dna
            with random_specimen_lock:
                random_specimen += 1
                
            yield {"dna" : dna, "parents" : [], "id" : next_id(), "guesses" : None, "wrong" : None, "winrate" : None}

        else:
            dna_i = int(triangular(0, len(r), 0))
            dnb_i = int(triangular(0, len(r), 0))
            dna = r[dna_i]
            dnb = r[dnb_i]

            dnc = merge_dna(dna["dna"], dnb["dna"])

            print "mated  %s & %s -> %s\n" % (dna["dna"], dnb["dna"], dnc)
            with mated_specimen_lock:
                mated_specimen += 1
            yield {"dna" : dnc, "parents" : ["%s (%s)" % (dna_link(dna["dna"]), dna["winrate"]),
                                             "%s (%s)" % (dna_link(dnb["dna"]), dnb["winrate"])],
                   "id" : next_id(), "guesses" : None, "wrong" : None, "winrate" : None}



sp = specimen_generator()



def merge_dna(dna, dnb):
    result = ""

    while len(dna) > 0 or len(dnb) > 0:
        if len(dna) > 0:
            a = str(dna[0])
            dna = dna[1:]

            if not a in result:
                result += a

        if len(dnb) > 0:
            b = str(dnb[0])
            dnb = dnb[1:]

            if not b in result:
                result += b

    return result


@decorator
def htmlify(f, *args, **kwargs):
    return """<html>
    <head>
    </head>
    <body>
        """ + f(*args, **kwargs) + """   </body>
</html>"""
    return toHTML
    
@decorator
def timings(f, *args, **kwargs):
    t = timer()
    return f(*args, **kwargs) + t.next()



## flask routes

app = Flask(__name__)

@app.route("/")
@htmlify
@timings
def index():
    return get_scores(get_results())

@app.route("/winrate")
@htmlify
@timings
def by_winrate():
    return get_scores(get_results_by_winrate())

@app.route("/wrong")
@htmlify
@timings
def by_wrong():
    return get_scores(get_results_by_wrong())

@app.route("/guesses")
@htmlify
@timings
def by_guesses():
    return get_scores(get_results_by_guesses())


@app.route("/info")
@htmlify
@timings
def get_info():
    return "mated: %s, random: %s, result_count: %s, job_count: %s" % (mated_specimen, random_specimen, len(get_results()), len(results))



@app.route("/dictionary")
def get_dictionary():
    return "\n".join(dictionary)


@app.route("/repetitions")
def repetitions():
    return "1000"


@app.route("/dna/<dna>")
def get_dna_score(dna):
    if results.has_key(dna):
        out = []
        for k,v in results[dna].iteritems():
            out.append("%s: %s" % (k, v))
        return "<br/>\n".join(out)
    else:
        return "not found" # should be 404


@app.route("/jobs/")
@timings
def show_jobs():
    with job_buffer_lock:
        return "%s jobs:\n" % len(job_buffer) + "\n".join([job["dna"] for job in job_buffer])


@app.route("/jobs/<int:job_count>")
def pop_jobs(job_count):
    t = timer()

    jobs = []
    while len(jobs) < job_count:
        pause = False
        with job_buffer_lock:
            if len(job_buffer) is 0:
                pause = True
            else:
                jobs.append(job_buffer.popleft()["dna"])
        if pause:
            sleep(0.001)

    print "dispatched %s jobs in %s" % (job_count, t.next())

    return "\n".join(jobs)


@app.route("/dna/<dna>/<float:guesses>/<float:wrong>/<float:winrate>")
@timings
def record_score(dna, guesses, wrong, winrate):    
    if results.has_key(dna):
        with results_lock:
            results[dna]["guesses"] = guesses
            results[dna]["wrong"] = wrong
            results[dna]["winrate"] = winrate
            with sync_buffer_lock:
                sync_buffer.append(results[dna])
    
        return "ok"
    else:
        return "unknown dna string"


@app.route("/resetDB")
def reset():
    pass


## setup

dictionary_file = open('dictionary.txt')
for line in dictionary_file:
    dictionary.append(line.strip())



def buffer_filler():
    sp = specimen_generator()
    while True:
        if len(job_buffer) < buffer_size:
            while len(job_buffer) < buffer_size:
                job = sp.next()
                with job_buffer_lock:
                    job_buffer.append(job)
                    results[job["dna"]] = job

        else:
            sleep(0.5)


def save_results():
    while True:
        if len(sync_buffer) > 0:
            with open("results.txt", 'a') as f:
                while len(sync_buffer) > 0:
                    print "sync'ing file"
                    with sync_buffer_lock:
                        f.write(json.dumps(sync_buffer.popleft())+"\n")

        else:
            sleep(1)


buffer_fillers = []
syncer = None


def monitor():
    global syncer
    while True:
        alive = 0
        for t in buffer_fillers:
            if t.is_alive():
                alive += 1

        for i in range(buffer_filler_count-alive):
            t = Thread(target=buffer_filler)
            t.start()
            buffer_fillers.append(t)

        print "Buffer fillers running: %s" % alive

        if syncer is None or syncer.is_alive() is not True:
            syncer = Thread(target=save_results)
            syncer.start()
        
        sleep(1)



if __name__ == "__main__":
    mon = Thread(target=monitor)
    mon.start()

    #app.run(host="172.31.25.155", debug=True)
    #app.run(debug=True)
    http_server = HTTPServer(WSGIContainer(app))
    http_server.listen(5000)
    IOLoop.instance().start()
