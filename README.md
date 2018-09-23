# Pvideo Dashboard 

### Requisiti

Le Pvideo Dashboard sono state realizzate per mostrare l’andamento della campagne marketing fatte mediante l’invio di un Doxee Pvideo.
Vengono mostrati dati come il numero dei video/email prodotti, il numero di email ricevute, il numero di video aperti, le iterazioni e le conversioni che l’utente ha cliccato.
Questi dati hanno lo scopo di mostrare al cliente in maniera aggregata l’andamento e l’efficacia della sua campagna.

### Campagna

La campagna è il concetto base delle Pvideo Dashboard. Equivale alla campagna marketing. 
Logicamente, sulle Pvideo Dashboard è identificata con un identificativo che corrisponde al Cliente e Procedura.
Ogni campagna può essere suddivisa in una o più versioni.

## Architettura

### Cloud Analytics
Cloud Analytics è il prodotto di Doxee da cui vengono letti i dati da mostrare sulle Pvideo Dashboard. 
Questo prodotto raccoglie eventi dalla piattaforma Doxee e dai dispositivi memorizzandoli in un Catalogo dati, implementato con un database Redshift di Amazon Web Service.

### Pvideo Dashboard
Le Pvideo Dashboard sono state implementate utilizzando tecnologie Amazon Web Services.
Le tecnologie che sono state utilizzate sono:
- Simple Storage Service (S3) : Hostano la webapplication UI e memorizzano i dati delle campagne.
- Lambda: Implementano tutte le logiche delle dashboard: autorizzazione, aggiornamento dati, recupero dati.
- CloudFront : Entry point di ogni chiamata, sia per la visualizzazione della UI, sia per le chiamate al backend
- DynamoDB: Memorizza le configurazione delle autorizzazioni e delle campagne
- API Gateway: Espone il REST che serve per visualizzare le campagne
- Cognito: Gestisce il login alla UI delle dashboard.
- Cloudwatch: Registra i log dell’applicazione e mediante Cloudwatch Events schedula l’aggiornamento dei dati delle campagne

### User interface
La user interface è realizzata utilizzando tecnologie HTML5 e comunica con il backend mediante chiamate REST. 
Viene esposta su internet mediante CloudFront che reindirizza la chiamata http su un bucket S3 sfruttando la feature “Static Website Hosting”. 
L’autenticazione è implementata mediante Amazon Cognito.
Cognito permette di creare utenti e dare loro delle autorizzazioni mediante gruppi.
Ogni gruppo verrà associato ad una o più campagne mediante configurazione sul database DynamoDB.

### Backend
Il backend è implementato da un endpoint REST implementato con Amazon Api Gateway esposto mediante CloudFront.
Vi sono due metodi:
- campaigns 
- getjson

#### campaigns
All’apertura della UI, subito dopo il login, viene fatta una GET “campaign” verso il backend. Questo metodo serve per popolare il menu delle Pvideo Dashboard e per restituire all’interfaccia l’elenco delle campagne che l’utente può visualizzare.
La chiamata è implementata con una AWS Lambda con runtime implementato in nodejs 6.10.
Vengono fatte delle query su DynamoDB per recuperare l’elenco delle campagne configurate per i gruppi al quale l’utente loggato appartiene.
Il risultato è un json che elenca le campagne divise tra test e produzione. In questo json vengono indicate anche le versioni delle singole campagne

#### getjson
Quando viene selezionata una campagna la UI fa una chiamata “getjson” POST verso il backend. Nella request vengono indicati la campagna che si vuole visualizzare, la versione della campagna e se si tratta di dati di test o di produzione.
La chiamata è implementata con una AWS Lambda con runtime implementato in nodejs 6.10.
Questa chiamata ha come risultato una signed url di cloudfront per scaricare il json contenente i dati da mostrare sulla UI.
La signed url è una feature di CloudFront che permette di restituire una URL firmata mediante un certificato. Quando il browser apre questa url manda una richiesta a CloudFront che la valida e fa scaricare il contenuto. Nel caso di una signature errata viene impedito il download della risorsa.

#### Configurazione campagne
Le configurazioni sono persistite sul database DynamoDB. Amazon DynamoDB è un database non relazionale che fornisce prestazioni affidabili su qualsiasi scala. Si tratta di un database multi master, multi regione e completamente gestito che fornisce latenza costante di pochi millisecondi e che offre sicurezza integrata, backup e ripristino e cache in memoria. 
Sono state create due tabelle:
-	PvideoDashboardCampaign
-	PvideoDashboardAuthorization

#### PvideoDashboardCampaign
Questa tabella contiene le campagne configurate sul sistema.
Ogni entry contiene il nome della campagna, l’indicazione delle versioni di test e di produzione, la data di ultimo aggiornamento.

#### PvideoDashboardAuthorization
Questa tabella contiene le configurazioni per i gruppi di Cognito che sono associati all’utente.
Ogni gruppo ha una entry con le campagne che il gruppo può vedere.

### Aggiornamento dati
I dati mostrati dalle Pvideo Dashboard sono salvati su S3 in json divisi per campagna, versione e scope (test – production).
La procedura di aggiornamento viene schedulata da una Rule di CloudWatch Events che funziona come un crontab lanciando il processo di aggiornamento ogni 5 minuti.
Questo processo è implementato con una AWS Lambda con runtime implementato in java 8.
La lambda estrae la lista delle campagne dalla tabella DynamoDB ed effettua le query di estrazioni dei dati sul database Redshift. Le query sono incrementali, ovvero vengono estratti solo gli eventi caricati dall’ultimo aggiornamento e vengono aggiornati/creati i json delle campagne su S3.
L’autorizzazione per la connessione al database Redshift è implementata mediante IAM Role, ovvero senza specificare nessuna username e password. 
La lambda di aggiornamento dati è eseguita con un IAM Role che può accedere a Redshift. 
Installazione

### Installazione
L’installazione delle Pvideo Dashboard avviene attraverso le automation di Doxee. Le automation di Doxee sono un prodotto sviluppato da Doxee che si basa su ansible, una tecnologia molto conosciuta per sviluppare automazioni IT.
E’ stato sviluppato un playbook (insieme di azioni) ansible per le pvideo dashboard che installa tutto quello che serve per far funzionare le dashboard. Viene sfruttata la tecnologia AWS Cloudformation, che permette di automatizzare l’installazione di tutti i componenti AWS scrivendo un solo file di testo scritto in un linguaggio YAML.
Il playbook ansible esegue i seguenti step:
-	Upload su un bucket S3 di tutti i file necessari (YAML del cloudformation, binari delle lambda)
-	Lancio di cloudformation mediante le librerie ansible
-	Upload della webapplication


[![N|Solid](https://i.creativecommons.org/l/by-nc-sa/4.0/88x31.png)](http://creativecommons.org/licenses/by-nc-sa/4.0/)
This work is licensed under a  [Creative Commons Attribution-NonCommercial-ShareAlike 4.0 International License][cc]


[cc]: <https://http://creativecommons.org/licenses/by-nc-sa/4.0/>

