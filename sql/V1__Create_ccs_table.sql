create table CCs (
    CCID varchar(100) not null,
    EMAILID varchar(100) not null,
    USERNAME varchar(100) not null,
    PRIMARY KEY(CCID),
    FOREIGN KEY(EMAILID)
);

