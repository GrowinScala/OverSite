create table Emails (
    EMAILID varchar(100) not null,
    CHATID varchar(100) not null,
    FROMADDRESS varchar(100) not null,
    DATEOF date not null,
    HEADER varchar(256) null, -- In an email a header is not a mandatory parameter
    BODY varchar(10000) null, -- In an email a body is not a mandatory parameter
    PRIMARY KEY(EMAILID),
    FOREIGN KEY(CHATID)
);



