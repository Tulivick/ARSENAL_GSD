# ARSENAL_GSD
**A**utomatic t**R**ust e**S**timator based on s**EN**timent an**AL**ysis for **GSD**

# Folders
We have four folder in this repository:

* Arsenal GSD GUI: this project contains a simple graphical user interface for ARSENAL-GSD, where you can run it or explore the source code in order to see how it works;
* ArsenalGSD: this project contais the ARSENAL-GSD framework, developed to use with GitHub;
* Libs: jar files needed to run both projects, Arsenal GSD GUI and ArsenalGSD, except SentiStrength.
* SentStrength_Data_Sept2011: Modified SentiStrength Data that is used by ARSENAL-GSD

# Running it

## Requirements

In order to run Arsenal GSD GUI and/or ArsenalGSD, you will need:

* SentiStrength Java Version: we could not add it to the Libs folder; however, you can obtain the corresponding .jar file from [SentiStrength website](http://sentistrength.wlv.ac.uk/). You can obtain it buying one license or, if it's for research purpose only ask a free copy throught your academic email.
* Credentials for Watson Natural Language Understanding (WNLU): if you already have an bluemix account with valid credentials you can use them. If you don't have a bluemix account, a free trial account can be obtained [here](https://www.ibm.com/watson/services/natural-language-understanding/). Create your account following the steps informed in the website than configure your region, organization and space in order to create a credential for Watson Natural Language Understanding. After create your credential go to "Service Credentials" and click on "View credentials" under Actions to see your username and password.

## Setup

### Arsenal GSD GUI

`ARSENAL_GSD/Arsenal GSD GUI/dist/` folder has everything you need. Just add SentiStrength `.jar` to the `lib` folder and place `SentStrength_Data_Sept2011` in the working directory. All done, just run:

```bash
java -jar Arsenal_GSD_GUI.jar
```

Fill in the fields:
* Git Login
  * User: GitHub username;
  * Pwd: GitHub password;
* Target Repository
  * Owner: repository owner of your GitHub target repository;
  * Repo: name of your GitHub target repository;
  * Time Intl: data time interval in days;
* Watson Natural Language Understanding
  * Username: WNLU credentials' username
  * Password: WNLU credentials' password
* Analysed Data: select the evidences you wish to consider.

Note that our GUI was created just to provide an example of Arsenal-GSD application, so it is very simple interface. It does not validate fields neither organize nodes and edges when you generate one of the graphs (Initial Relations, Relations or Trust), they are all stacked.

### ArsenalGSD

Add all ARSENAL-GSD dependencies to your project then you can call it like the following example:

```java
TrustFramework tf = new TrustFramework(); // instantiate it
tf.setFactory(new TFGraphImpFactory()); 
tf.setdExtractor(new GitHubExtractor("username", *******)); // set GitHub credentials
tf.setTimeInterval(60);
//repository information
tf.setOwner("mpociot");
tf.setRepository("laravel-apidoc-generator");
// add evidences with their weight
tf.addEvidence(new ConversationMimicry(1));
tf.addEvidence(new Colaboration(1));
tf.addEvidence(new KnowledgeAcceptance(1));
tf.addEvidence(new TaskDelegation(1));
tf.addEvidence(new ProfileComunality(1));
tf.addEvidence(new MessageSentiment(1,"./SentStrength_Data_Sept2011/","*****-*****-******-*****", "*******")); //parameter are: weight, SentiStrength Data folder, WNLU username, WNLU password
JFrame frame = new JFrame("Grafo de confiança");
JGraph jGraph = new JGraph(new JGraphModelAdapter<>(((TFGraphImp) tf.getTrustGraph()).getGraph()));
frame.getContentPane().add(jGraph);
frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
frame.setSize(400, 320);
frame.setVisible(true);
```

# License
<a rel="license" href="http://creativecommons.org/licenses/by/4.0/"><img alt="Creative Commons License" style="border-width:0" src="https://i.creativecommons.org/l/by/4.0/88x31.png" /></a><br />This work is licensed under a <a rel="license" href="http://creativecommons.org/licenses/by/4.0/">Creative Commons Attribution 4.0 International License</a>.
