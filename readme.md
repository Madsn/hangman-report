Server
======

The server is written in python, and depends on the following
extra packages, which can be installed with easy_install:
- flask, tornado, decorator

The server can be launched as by double-clicking dispatch.py,
if python is installed and added to the PATH. It should then
start creating jobs, and be accessible on all interfaces
and listening at port 5000.

While the server is running, the following URLs can be accessed:
- http://address:5000/
-: Sorted list of the results. It will get slow over time given
   the amount of test results to show.

- http://address:5000/info
-: stats about number of random and mated DNA strings and results

- http://address:5000/jobs
-: list of jobs in the job buffer

- http://address:5000/jobs/n (where n is an integer)
-: pops n items from the job buffer


Client
======

The client requires a running server to work, and if the server
isn't running on localhost (127.0.0.1) the address will have to
be changed in Utils.java on line 11.

To compile and run the Worker without a Java IDE, you will need to 
have java added to your system PATH.
Compiling and running in windows can then be done via CMD:

	1. navigate to the "Worker" folder
	2. compile the java files: javac *.java
	3. run the worker: java Worker


Test Results
============

Warning: The HTML-files containing the test-results can take
         a long time to open, and cannot be sorted like
		 when running the server.

"no_skip.html" contains data for when skipping letters was
disabled, and "skip.html" contains data for when skipping letters.

