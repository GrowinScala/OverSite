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
    SENT boolean,
    TRASH boolean,
    PRIMARY KEY(EMAILID),
    FOREIGN KEY(CHATID) REFERENCES Chats(CHATID)
);
create table CCs (
    CCID varchar(100) not null,
    EMAILID varchar(100) not null,
    USERNAME varchar(100) not null,
    TRASH boolean,
    PRIMARY KEY(CCID),
    FOREIGN KEY(EMAILID) REFERENCES Emails(EMAILID)
);


create table BCCs (
    BCCID varchar(100) not null,
    EMAILID varchar(100) not null,
    USERNAME varchar(100) not null,
    TRASH boolean,
    PRIMARY KEY(BCCID),
    FOREIGN KEY(EMAILID) REFERENCES Emails(EMAILID)
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


create table ToAddresses (
    TOID varchar(100) not null,
    EMAILID varchar(100) not null,
    USERNAME varchar(100) not null,
    TRASH boolean,
    PRIMARY KEY(TOID),
    FOREIGN KEY(EMAILID) REFERENCES Emails(EMAILID)
);

create table Shares (
    SHAREID varchar(100) not null,
    CHATID varchar(100) not null,
    FROMUSER varchar(100) not null,
    TOID varchar(100) not null,
    PRIMARY KEY(SHAREID),
    FOREIGN KEY(CHATID) REFERENCES Chats(CHATID)
);




