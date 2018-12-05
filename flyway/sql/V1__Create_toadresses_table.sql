create table ToAddresses (
    TOID varchar(100) not null,
    EMAILID varchar(100) not null,
    USERNAME varchar(100) not null,
    PRIMARY KEY(TOID),
    FOREIGN KEY(EMAILID)
);

