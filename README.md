# Mobile-Cloud-Computing-Project
**Contributors: Giovanni Armano, Luigi di Girolamo.**

A RESTful web service for a **CALENDAR** developed for the "Mobile Cloud Computing" course at the "Aalto University" in Espoo.


Since we are talking about a calendar, you can register a new user, create/edit calendars and events, share an event. Since it's RESTful, GET POST PUT and DELETE are all implemented.

The goal of the course was developing a complete web service for a **CALENDAR** and then deploying it on a OpenStack machine (provided by the Aalto University).<br>
First step: developing a backend. The choice was free but we chose the suggested technologies: NodeJS and MongoDB. We followed the current trend that is using Non-Relational Databases.<br>
In order to run the BackEnd you need to install the NodeJS package and related plugins (like npm, express) as well as MongoDB.<br><br>

Second step: developing the FrontEnd. HTML + CSS + JS and jQuery. This application fetches and posts data to the backend. Moreover, it's possible to synchronize your events with Google: import and export events from the frontend to google cal and vice versa. To perform this operation, a 3rd party login has been implemented.<br><br>

Third step: android application. Android Studio with SDK. The app talks with the backend (exactly like the frontend).<br>
You can perform the same operations of the front end and it is possible to sync the app events with the phone's calendar (NOTICE: not with the Google Account).
