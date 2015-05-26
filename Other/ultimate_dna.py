dictionary = []
for line in open('dictionary.txt'):
    dictionary.append(line.strip())

az = map(lambda x: x+97, range(26))
az = map(chr, az)

results = {}

for c in az:
    results[c] = 0
    for w in dictionary:
        if c in w: results[c] += 1

print "".join([k for k,v in sorted(results.iteritems(), key=lambda x: x[1], reverse=True)])
    
