create table country
(
    id   identity not null primary key,
    name varchar  not null
);

create table film
(
    id         identity                                       not null primary key,
    country_id bigint                                         not null,
    title      varchar                                        not null,
    genre      enum ('DRAMA', 'COMEDY', 'THRILLER', 'HORROR') not null,
    tags       array                                          not null,
    constraint fk_film_country foreign key (country_id)
        references country (id) on delete cascade
);

create table actor
(
    id         identity                not null primary key,
    country_id bigint                  not null,
    first_name varchar                 not null,
    last_name  varchar                 null,
    birthday   date                    not null,
    gender     enum ('MALE', 'FEMALE') not null,
    tags       array                   not null,
    constraint fk_actor_country foreign key (country_id)
        references country (id) on delete cascade
);

create table film_actor
(
    film_id  bigint not null,
    actor_id bigint not null,
    constraint pk_film_actor primary key (film_id, actor_id),
    constraint fk_film_actor_film foreign key (film_id)
        references film (id) on delete cascade,
    constraint fk_film_actor_actor foreign key (actor_id)
        references actor (id) on delete cascade
);

insert into country(id, name)
values (0, 'Argentina'),
       (1, 'Australia'),
       (2, 'Austria'),
       (3, 'Belgium'),
       (4, 'Brazil'),
       (5, 'Canada'),
       (6, 'Finland'),
       (7, 'France'),
       (8, 'Germany'),
       (9, 'Italy'),
       (10, 'Japan'),
       (11, 'New Zealand'),
       (12, 'Norway'),
       (13, 'Portugal'),
       (14, 'Russia'),
       (15, 'Spain'),
       (16, 'Sweden'),
       (17, 'United Kingdom'),
       (18, 'USA');

insert into film(id, country_id, title, genre, tags)
values (0, 7, 'Amelie', 'COMEDY', ('best', 'audrey')),
       (1, 7, 'A Very Long Engagement', 'DRAMA', ('audrey')),
       (2, 7, 'Hunting and Gathering', 'DRAMA', ('audrey')),
       (3, 7, 'Priceless', 'COMEDY', ('audrey')),
       (4, 17, 'House', 'COMEDY', ('best', 'house')),
       (5, 17, 'Peter''s Friends', 'COMEDY', ('house')),
       (6, 17, 'Street Kings', 'THRILLER', ('house')),
       (7, 17, 'Mr. Pip', 'DRAMA', ('house')),
       (8, 18, 'Ocean''s Eleven', 'THRILLER', ('best', 'julia', 'clooney')),
       (9, 18, 'Stepmom', 'DRAMA', ('julia')),
       (10, 18, 'Pretty Woman', 'COMEDY', ('julia')),
       (11, 18, 'From Dusk Till Dawn', 'THRILLER', ('clooney'));

insert into actor(id, country_id, first_name, last_name, birthday, gender, tags)
values (0, 7, 'Audrey', 'Tautou', '1976-08-09', 'FEMALE', ('best', 'audrey')),
       (1, 7, 'Mathieu', 'Kassovitz', '1967-08-03', 'MALE', ()),
       (2, 7, 'Jamel', 'Debbouze', '1975-06-18', 'MALE', ('best')),
       (3, 7, 'Dominique', 'Pinon', '1955-03-04', 'MALE', ()),
       (4, 7, 'Gaspard', 'Ulliel', '1984-11-25', 'MALE', ()),
       (5, 7, 'Guillaume', 'Canet', '1973-04-10', 'MALE', ()),
       (6, 7, 'Gad', 'Elmaleh', '1971-04-19', 'MALE', ()),
       (7, 17, 'Hugh', 'Laurie', '1959-06-11', 'MALE', ('best', 'house')),
       (8, 17, 'Stephen', 'Fry', '1957-08-24', 'MALE', ('best')),
       (9, 18, 'Keanu', 'Reeves', '1964-09-02', 'MALE', ()),
       (10, 18, 'Julia', 'Roberts', '1967-10-28', 'FEMALE', ('best', 'julia')),
       (11, 18, 'George', 'Clooney', '1967-10-28', 'MALE', ('best', 'clooney')),
       (12, 18, 'Brad', 'Pitt', '1963-12-18', 'MALE', ()),
       (13, 18, 'Susan', 'Sarandon', '1946-10-04', 'FEMALE', ()),
       (14, 18, 'Richard', 'Gere', '1949-08-31', 'MALE', ()),
       (15, 18, 'Salma', 'Hayek', '1966-09-02', 'FEMALE', ());


insert into film_actor(film_id, actor_id)
values (0, 0),
       (0, 1),
       (0, 2),
       (0, 3),
       (1, 0),
       (1, 3),
       (1, 4),
       (2, 0),
       (2, 5),
       (3, 0),
       (3, 6),
       (4, 7),
       (5, 7),
       (5, 8),
       (6, 7),
       (6, 9),
       (7, 7),
       (8, 10),
       (8, 11),
       (8, 12),
       (9, 10),
       (9, 13),
       (10, 10),
       (10, 14),
       (11, 11),
       (11, 15);
