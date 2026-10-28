# s-CPD: Student Co-Curricular Points and Development System Proposal Report

## 1\. Introduction

The Student Co-Curricular Points and Development (s-CPD) system is an Android application designed to make it easier to manage student activities, event bookings, attendance, and co-curricular points. The system has three types of users: students, club committee members, and s-CPD administrators. Students can browse approved events, book places, record attendance, and check their points history. Club committee members can create and manage events, request points for activities, and track participation, while administrators review and approve events and check attendance and points records. The system's main goal is to make sure s-CPD points are awarded fairly, even when the internet connection is unreliable. 

This proposal describes the system's requirements, architecture, and design as a foundation for its future development and testing.

## 2\. Project Aims and Objectives

### 2.1 Aim

The aim of the s-CPD project is to develop an Android application that simplifies the management of student activities, bookings, attendance, and points while keeping records accurate and secure, even when the internet connection is unstable.

### 2.2 Objectives

The project aims to achieve the following objectives:

1. Allow club committee members to create and manage events and request points, administrators to approve or reject events, and students to find approved events, book available places, avoid schedule clashes and receive event notifications.

2. Use QR codes, biometric verification and registered devices to prevent fake or unauthorised check-ins, while allowing students to record attendance offline and upload it when the connection returns, see the status of each record and why it was rejected, and have suspicious records or special cases reviewed.

3. Award points only for verified attendance, and keep a record of every point awarded, changed or removed, with the reason for each change.

4. Give students, club committee members and administrators separate permissions to prevent unauthorised actions and changes to records.

## 3\. Proposal Structure

The following sections explain the proposed s-CPD system in more detail:

- [Requirements Analysis and User Stories](User%20Stories.md)

- [System Architecture and Diagrams](system-architecture.md)
  
- [App Design and UI States](Design.md)

## 4\. Conclusion

In conclusion, the proposed s-CPD system gives students, club committees and administrators a single platform for managing events, bookings, attendance and co-curricular points. Verified check-ins, offline attendance recording and a full history of points changes help ensure that points are awarded fairly and only for activities students have actually attended. The requirements, architecture and design set out in this proposal will guide the development and testing of a more reliable and accountable co-curricular management system.
