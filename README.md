# The Teiid WebUI project

## Summary

This is the working Git repository for the Teiid WebUi project.

This project contains the Teiid WebUi based on the Uberfire framework

## Get the code

The easiest way to get started with the code is to [create your own fork](http://help.github.com/forking/) of this repository, and then clone your fork:

	$ git clone git@github.com:<you>/teiid-webui.git
	$ cd teiid-webui
	$ git remote add upstream git://github.com/teiid-designer/teiid-webui.git
	
At any time, you can pull changes from the upstream and merge them onto your master:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git push origin                   # pushes all the updates to your fork, which should be in-sync with 'upstream'

The general idea is to keep your 'master' branch in-sync with the 'upstream/master'.

## Building teiid-webui

We use Maven 3.x to build our software. The following command compiles all the code, installs the JARs into your local Maven repository, and runs all of the unit tests:

	$ mvn clean install -s settings.xml

## Running teiid-webui

    $ cd teiid-webui-webapp
    $ mvn gwt:run

Login

    admin / admin

## Contribute fixes and features

teiid-webui is open source, and we welcome anybody who wants to participate and contribute!

If you want to fix a bug or make any changes, please log an issue in the [Teiid WebUI JIRA](https://issues.jboss.org/browse/TEIIDWEBTL) describing the bug or new feature. Then we highly recommend making the changes on a topic branch named with the JIRA issue number. For example, this command creates a branch for the TEIIDWEBTL-1234 issue:

	$ git checkout -b teiidwebtl-1234

After you're happy with your changes and a full build (with unit tests) runs successfully, commit your changes on your topic branch
(using really good comments). Then it's time to check for and pull any recent changes that were made in the official repository:

	$ git checkout master               # switches to the 'master' branch
	$ git pull upstream master          # fetches all 'upstream' changes and merges 'upstream/master' onto your 'master' branch
	$ git checkout teiidwebtl-1234      # switches to your topic branch
	$ git rebase master                 # reapplies your changes on top of the latest in master
	                                      (i.e., the latest from master will be the new base for your changes)

If the pull grabbed a lot of changes, you should rerun your build to make sure your changes are still good.
You can then either [create patches](http://progit.org/book/ch5-2.html) (one file per commit, saved in `~/teiidwebtl-1234`) with 

	$ git format-patch -M -o ~/teiidwebtl-1234 orgin/master

and upload them to the JIRA issue, or you can push your topic branch and its changes into your public fork repository

	$ git push origin teiidwebtl-1234         # pushes your topic branch into your public fork of Teiid Web UI

and [generate a pull-request](http://help.github.com/pull-requests/) for your changes. 

We prefer pull-requests, because we can review the proposed changes, comment on them,
discuss them with you, and likely merge the changes right into the official repository.

