
This project achieves all three sections of this homework.
The first section: We can intercept the message which Bob sent to Alice. To show the outcome clearly, the screen will stop about 5 seconds.
The second section:Changing the sender as cgy, and send the message to Alice, and ensure this message can be decrypted by Alice. Besides, this step is also requested to change the content of message, I finished this part in the step3. 
The third section:By changing the padding and message’s content, we can get each byte from the end of the plaintext to the beginning part. Because this step is quite time-consuming I just achieve guessing the last 7 bytes and the last 4-byte CRC. The outcome is showed block by block.
This step is based on step2 and step3, which means if this step can run successfully that step1 and2 can run successfully as well.Besides, this step will continue output each block’s information

Step1	Environment:Eclipse. New a project. Start docker and jmessage server.
step2   

Step2	New a package named ”guang.client” into src.

Step3	Copy all the .java files,the Manifest.MF and gson-2.7.jar into “guang.client”

Step4	These errors may occur.
	1)err1: fail to find out com.google.gson.Gson. Countermeasure：copy gson-2.7.jar 	into this project, and then right click->build path->configure build path-> 		libraries->add JARS->import json-2.7.jar;
	2)error2: fail to find out sun.misc.BASE64Decoder and sun.misc.BASE64Encoder. 		Countermeasure:right click this project->build path->configure build path-> 		libraries->remove the current JRE System Library->apply->add Library->JRE System 	Library->workspace default JRE->finish->apply
	3）When you run this program, sometimes it will occur ArrayIndexOutOfBoundsException，because the first message is short than expected, if this exception occurs, just restart the program.


Step5 	Copy gson2.7.jar under the same directory with the *.jar file exported at step5. 	Use bash going into the directory where *.jar is, execute “java -jar *.jar”. Then 	you can see my work.

