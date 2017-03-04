
Step1	Environment:Eclipse.New a project.

Step2	New a package named ”guang.client” into src.

Step3	Copy all the .java files,the Manifest.MF and gson-2.7.jar into “guang.client”

Step4	These errors may occur.
	1)err1: fail to find out com.google.gson.Gson. Countermeasure：copy gson-2.7.jar 	into this project, and then right click->build path->configure build path-> 		libraries->add JARS->import json-2.7.jar;
	2)error2: fail to find out sun.misc.BASE64Decoder and sun.misc.BASE64Encoder. 		Countermeasure:right click this project->build path->configure build path-> 		libraries->remove the current JRE System Library->apply->add Library->JRE System 	Library->workspace default JRE->finish->apply

Step5	After these steps above, java can compile this project automatically. Then we can 	export a JAR file(I have exported one, but if you want to export one by yourself, 	I write the method here in case.)Copy Mainfest.MF into root directory,right click 	this project->export->java->JAR file->select this project and determine the path 	and file name->next->next->Use existing manifest from workspace,find out the 		Manifest.MF I supply->finish.

Step6 	Copy gson2.7.jar under the same directory with the *.jar file exported at step5. 	Use bash going into the directory where *.jar is, execute “java -jar *.jar -s 		<server host> -p <server port> -u <user name>”. Then you can see my work.
