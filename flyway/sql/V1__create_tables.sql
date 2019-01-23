create table Chats (
    CHATID varchar(100) not null,
    HEADER varchar(256) not null,
    PRIMARY KEY(CHATID)
);

create table Emails (
    EMAILID varchar(100) not null,
    CHATID varchar(100) not null,
    FROMADDRESS varchar(100) not null,
    DATEOF date not null,
    HEADER varchar(256) null, -- In an email a header is not a mandatory parameter
    BODY varchar(10000) null, -- In an email a body is not a mandatory parameter
    TRASH boolean not null,
    PRIMARY KEY(EMAILID),
    FOREIGN KEY(CHATID) REFERENCES Chats(CHATID)
);

create table EmailsDestination(
	EMAILID varchar(100) not null,
    USERNAME varchar(100) not null,
    DESTINATION ENUM('to', 'cc', 'bcc'),
    TRASH boolean not null,
    FOREIGN KEY(EMAILID) REFERENCES Emails(EMAILID)
);

create table Drafts (
    DRAFTID varchar(100) not null,
    CHATID varchar(100) null,
    USERNAME varchar(100) not null,
    DATEOF date not null,
    HEADER varchar(256) null, -- In an email a header is not a mandatory parameter
    BODY varchar(10000) null, -- In an email a body is not a mandatory parameter
    TRASH boolean,
    PRIMARY KEY(DRAFTID)
);

create table DraftsDestination (
    DRAFTID varchar(100) not null,
    USERNAME varchar(100) not null,
    DESTINATION ENUM('to', 'cc', 'bcc'),
    FOREIGN KEY(DRAFTID) REFERENCES Drafts(DRAFTID)
);

create table Users (
    USERNAME varchar(100) not null,
    PASSWORD varchar(100) not null,
    PRIMARY KEY(USERNAME)
);


create table Logins (
    USERNAME varchar(100) not null,
    token varchar(100) not null,
    validDate BigInt not null,
    active boolean not null,
    FOREIGN KEY(USERNAME) REFERENCES Users(USERNAME)
);

create table Shares (
    SHAREID varchar(100) not null,
    CHATID varchar(100) not null,
    FROMUSER varchar(100) not null,
    TOUSER varchar(100) not null,
    PRIMARY KEY(SHAREID),
    FOREIGN KEY(CHATID) REFERENCES Chats(CHATID)
);




