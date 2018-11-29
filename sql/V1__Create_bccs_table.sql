create table BCCs (
    BCCID varchar(100) not null,
    EMAILID varchar(100) not null,
    USERNAME varchar(100) not null,
    PRIMARY KEY(BCCID),
    FOREIGN KEY(EMAILID)
);

