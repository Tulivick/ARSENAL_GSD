# ARSENAL_GSD
**A**utomatic t**R**ust e**S**timator based on s**EN**timent an**AL**ysis for **GSD**

# Folders
We have four folders in this repository:

* Arsenal GSD GUI: this project contains a simple graphical user interface for ARSENAL-GSD, where you can run it or explore the source code in order to see how it works;
* ArsenalGSD: this project contains the ARSENAL-GSD framework, developed to use with GitHub;
* Libs: jar files needed to run both projects, Arsenal GSD GUI and ArsenalGSD, except SentiStrength.
* SentStrength_Data_Sept2011: Modified SentiStrength Data that is used by ARSENAL-GSD

# Running it

## Requirements

In order to run Arsenal GSD GUI and/or ArsenalGSD, you will need:

* SentiStrength Java Version: we could not add it to the Libs folder; however, you can obtain the corresponding .jar file from [SentiStrength website](http://sentistrength.wlv.ac.uk/). You can obtain it by buying one license or, if it will be used for research purpose only, ask for a free copy through your academic e-mail.
* Credentials for Watson Natural Language Understanding (WNLU): if you already have a bluemix account with valid credentials, you can use them. If you don't have a bluemix account, a free trial account can be obtained [here](https://www.ibm.com/watson/services/natural-language-understanding/). Create your account following the steps informed in the website, and then configure your region, organization and space in order to create a credential for Watson Natural Language Understanding. After creating your credentials, go to "Service Credentials" and click on "View credentials" under Actions to see your apikey and url.

## Setup

### Arsenal GSD GUI

`ARSENAL_GSD/Arsenal GSD GUI/dist/` folder has everything you need to run GUI. Just add SentiStrength `.jar` to the `lib` folder and place `SentStrength_Data_Sept2011` in the working directory. All done, just run:

```bash
java -jar Arsenal_GSD_GUI.jar
```

In the application window, fill in the fields:
* Git Login
  * User: your GitHub username;
  * Pwd: your GitHub password;
* Target Repository
  * Owner: username of the repository owner of your GitHub target repository;
  * Repo: name of your GitHub target repository;
  * Time Intl: data time interval in days;
* Watson Natural Language Understanding
  * WNLU credentials for the service (NOT the credentials for IBM)
      * Apikey
      * Url
* Analysed Data: select the evidences you wish to be considered in the estimations.

Press one of the buttons to generate the corresponding graph (Initial Relations, Relations or Trust). Note that our GUI was created just to provide an example of Arsenal-GSD's application, so it is very simple. It does not validate entry fields neither organize nodes and edges when graphs are generated -- they will be stacked one upon the other. Thus, to visualize the resulting graph, use your mouse to reposition its nodes and edges.

### ArsenalGSD

Add all ARSENAL-GSD dependencies to your project. Then, you can call it like the following example:

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
