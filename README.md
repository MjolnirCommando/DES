# DES
Decentralized Election System (Cutler-Bell Prize Scholarship Entry)

Created by Matthew Edwards, October 15, 2015 as an entry for the Cutler-Bell Prize Scholarship.

## Links

[Cutler-Bell Prize Scholarship](http://www.csta.acm.org/Advocacy_Outreach/sub/Cutler-BellPrize.html)

[GitHub Repository](https://github.com/MjolnirCommando/DES)

[GitHub Website](http://mjolnircommando.github.io/DES/)

[Google Slides Presentation](https://docs.google.com/presentation/d/1Hp62kSNlg7fR3QsJYX39fGhO48mPnoTNf57le9FmlSI/edit?usp=sharing)

[Java Documentation](http://mjolnircommando.github.io/DES/doc/)


## How to Use

### Download

Download the JAR File (http://mjolnircommando.github.io/DES/DES.jar). This JAR File is runnable on its own, but for full functionality, it should be run from the command-line. To see the code or clone the repository, go to https://github.com/MjolnirCommando/DES .

### Application Commands

A running Node is able to accept several commands. These commands should only be used by advanced users.

(Parentheses) indicate an optional parameter

&lt;Braces&gt; indicate a required parameter

| Command          | Usage                                               | Description
|------------------|-----------------------------------------------------|--------------
| ```stop```       | ```stop```                                          | Stops the Node, saving the BlockChain to "data.block"
| ```ping```       | ```ping <IP ADDRESS>```                             | Pings a connected peer Node
| ```connect```    | ```connect (IP ADDRESS)```                          | Connects to a peer Node. If no address is specified, the Node will connect to a Node on the localhost with a port number one less than the current Node.
| ```getaddr```    | ```getaddr <IP ADDRESS>```                          | Sends a GETADDR packet to the specified peer Node
| ```myaddr```     | ```myaddr```                                        | Prints this Node's address
| ```addr```       | ```addr```                                          | Prints the addresses of all known peer Nodes
| ```testload```   | ```testload (BLOCKS) (BALLOTS) (BLOCKCHAIN FILE)``` | Performs a saving load test on this Node. For advanced users only




### Run from the Command-Line

The Launcher class accepts several command-line arguments through its main method.

In order to run properly, DES requires its own working directory which can be specified with ```-dir <Directory>```. DES will automatically create the specified directory and populate it with its working files. If no directory is specified, it will use the user's Desktop and create a new folder called "DES".

DES also requires several generated files in this directory, such as "data.block". If you are attempting to run the application in demonstration mode, it must also have an ID database.


#### Starting from Scratch

When starting from scratch, the JAR File must be run from the command-line in order to generate the necessary files:

1) ```java -jar DES.jar -dir DIRECTORY -gen```
   
   Generates "data.block" in the specified directory, then exits.

2) ```java -jar DES.jar -dir DIRECTORY -genids NUMBER_OF_IDS DIRECTORY```
   
   Generates ID Databases (so the Election Authority can be simulated).

3) ```java -jar DES.jar -demo -peer PEER_IP```
   
   Starts a Node in demonstration mode. It will load the public.data database file generated in step 2. The Node will attempt to connect to the specified peer. (Specifying a peer is optional).

4) ```java -jar DES.jar -demo -submit TIME -peer PEER_IP```
   
   Starts a Node in demonstration mode. It will load the private.data database file generated in step 2. It will submit Ballots within the specified timeframe (so the Election Application can be simulated). The Node will attempt to connect to the specified peer. (Specifying a peer is optional).

#### Command-Line Arguments

http://mjolnircommando.github.io/DES/doc/me/edwards/des/Launcher.html#main(java.lang.String[])

(Parentheses) indicate an optional parameter

&lt;Braces&gt; indicate a required parameter

| Flag          | Usage                                     | Description
|---------------|-------------------------------------------|--------------
| ```-count```  | ```-count (BlockChain File)```            | Tabulates the results of the specified BlockChain after loading it from file. If no BlockChain is specified, the default BlockChain is used.
| ```-demo```   | ```-demo```                               | Starts the Node in demonstration mode.
| ```-dir```    | ```-dir <Directory>```                    | Sets the working directory of the Node.
| ```-gen```    | ```-gen```                                | Generates a Genesis Block and saves it to "generated_blockchain.block" in the working directory.
| ```-genids``` | ```-genids <Number of IDs> (Directory)``` | Generates key databases for demonstration purposes.
| ```-name```   | ```-name <Name>```                        | Sets the human-readable name of the Node.
| ```-peer```   | ```-peer <Peer>```                        | Adds an initial peer to the Node which will be contacted during the Bootstrapping process.
| ```-port```   | ```-port <Port>```                        | Sets the port to be used by the Node.
| ```-submit``` | ```-submit (Time in seconds)```           | Adds a Submitter to the Node for demonstration purposes. If a time is specified, it will submit the available number of Ballots within that timeframe





## Documentation

Find the Java documentation at http://mjolnircommando.github.io/DES/doc/




## About

### Vision and Design Process

As I graduate from high school, one of my new responsibilities will be voting. I am very politically informed, and I credit this to my experience with debate throughout high school. Voting is something I hold in high regard; however, I realize how often most people do not exercise their right to vote. In the United States, regional elections are considered very successful if close to half of the citizenry participate. Federal elections often see just over half of eligible citizens casting ballots. This was puzzling to me and led me to inquire why people did not vote, even though it is a civic responsibility and gives each person a voice in government. What I found was that generally younger voters, voters who have non-professional jobs, and voters who experience economic hardship are less likely to exercise their right to vote. For me this posed a pressing ethical dilemma: why and how does the election system cause the disenfranchisement of the young and the underprivileged? The ability to vote should be readily available to everyone who has a right to vote. I propose a new strategy for internet voting that will encourage and enable all citizens to participate, providing a solution which would change the way people vote in a democracy. Computing already plays a major role in today’s society, and I believe that computer science in the future will not only advance technology, but also provide solutions to social problems.
  
I was inspired by Bitcoin’s security, reliability, and accessibility to novice users. Cryptography is what allows your credit card information to be transferred to your bank over the internet without a third party stealing your identity or information. The processes used by Bitcoin can be adapted, and other principles of cryptography can be used, to create a secure form of voting that maintains individual voter privacy.  Ballots can be written by voters in an Election Application, such as a browser application, and then broadcast to a network of Miner nodes. These nodes store Ballots using cryptographic techniques in Blocks to ensure that they are impossible to change. Blocks are kept in a linked list called the BlockChain.  All votes are public, but anonymous, so one can check their vote after it has been mined, but no third party could read a specific individual’s vote. This solution maintains one’s privacy and one’s vote security while allowing all voters to cast their ballots on election day regardless of any hurdles posed by normal voting, enabling widespread internet voting in the US and abroad.
  
Computer science as a discipline has many goals, but they are not limited to solving technological problems. In addition, computer science also can be used to solve critical social problems--to improve people’s lives and further our cultural ethos. This project contributes to the computer science by using it to change the way people engage in a fundamental aspect of our democratic society, one that has remained unchanged for hundreds of years.

### Strategy and Implementation

My strategy for internet voting relies on maintaining a balance among all entities involved, preventing any one entity from determining the election’s outcome. There are four stages in my Decentralized Election System: Voting, Mining, Review, and Result. To implement this strategy, I first determined the entities required: an Election Authority to manage voter identification; Election Applications for casting Ballots; Miner Nodes to record votes; and a mechanism for voters to verify their votes (the public BlockChain). After this, I focused on creating the Miner Node program as my artifact because it is the most complex and important component.
  
I first created a networking protocol so Nodes could communicate, allowing the system to be decentralized. Any entity can operate a Miner Node without changing the network’s functionality. Creating my own networking protocol was the most difficult part of this project because I had to create solutions to save and transmit large volumes of data. I initially made mistakes that caused data to become corrupted or lost. After further development, I was able to design a compressed format to save Ballots and Blocks efficiently and create a network that could reliably propagate data. I also had to create analogs of the Election Authority and Election Application to test my artifact.
  
Maintaining the security of one’s vote is not easy. I had to design a new strategy to prevent Ballots from being altered between the instant they are cast and when they are permanently recorded in the BlockChain. I based the strategy on anonymous authentication. When Ballots are cast, they are signed with a digital signature and possess a generated identifier. In the Review stage, an individual may check their Ballot in the BlockChain using this identifier; however, no third party can check any specific individual’s vote, keeping it anonymous. If the vote were to be altered before reaching a Miner Node, it would be declared invalid because the signature would not match. The biggest challenge of the validation strategy was its lack of precedence, so I used my own knowledge of cryptography to create a solution.
  
For the Mining algorithm, I used many of the techniques that Bitcoin uses to store transactions, adapting them to store Ballots. After enough Ballots are validated by a Node, they are “mined” into a Block. This process permanently records them in the public BlockChain: a linked-list of Blocks saved by every Node. I adapted Bitcoin’s Proof of Work mechanism for Blocks, using hashes to prevent changes to mined Blocks. Producing valid Blocks requires a large amount of processing power by design, but after Blocks are broadcast to the network, validating a Block’s hash is very fast. Creating an algorithm to append Blocks to the BlockChain after they are received and validated proved to be a hard problem. I had to redesign the original Bitcoin algorithm in order to reliably add Blocks to the BlockChain and allow Nodes to come to a consensus as Bitcoin’s original solution could not handle the volume of data for my network.


(C) Copyright 2015 by Matthew Edwards
