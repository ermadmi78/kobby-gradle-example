create table country
(
    id   bigint generated always as identity not null primary key,
    name varchar  not null
);

create table film
(
    id         bigint generated always as identity            not null primary key,
    country_id bigint                                         not null,
    title      varchar                                        not null,
    genre      enum ('DRAMA', 'COMEDY', 'THRILLER', 'HORROR') not null,
    tags       varchar                                        not null,
    constraint fk_film_country foreign key (country_id)
        references country (id) on delete cascade
);

create table actor
(
    id         bigint generated always as identity     not null primary key,
    country_id bigint                                  not null,
    first_name varchar                                 not null,
    last_name  varchar                                 null,
    birthday   date                                    not null,
    gender     enum ('MALE', 'FEMALE')                 not null,
    tags       varchar                                 not null,
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

insert into country(name)
values ('Argentina'),
       ('Australia'),
       ('Austria'),
       ('Belgium'),
       ('Brazil'),
       ('Canada'),
       ('Finland'),
       ('France'),
       ('Germany'),
       ('Italy'),
       ('Japan'),
       ('New Zealand'),
       ('Norway'),
       ('Portugal'),
       ('Russia'),
       ('Spain'),
       ('Sweden'),
       ('United Kingdom'),
       ('USA');

insert into film(country_id, title, genre, tags)
values (8, 'Amelie', 'COMEDY', 'best, audrey'),
       (8, 'A Very Long Engagement', 'DRAMA', 'audrey'),
       (8, 'Hunting and Gathering', 'DRAMA', 'audrey'),
       (8, 'Priceless', 'COMEDY', 'audrey'),
       (18, 'House', 'COMEDY', 'best, house'),
       (18, 'Peter''s Friends', 'COMEDY', 'house'),
       (18, 'Street Kings', 'THRILLER', 'house'),
       (18, 'Mr. Pip', 'DRAMA', 'house'),
       (19, 'Ocean''s Eleven', 'THRILLER', 'best, julia, clooney'),
       (19, 'Stepmom', 'DRAMA', 'julia'),
       (19, 'Pretty Woman', 'COMEDY', 'julia'),
       (19, 'From Dusk Till Dawn', 'THRILLER', 'clooney');

insert into actor(country_id, first_name, last_name, birthday, gender, tags)
values (8, 'Audrey', 'Tautou', '1976-08-09', 'FEMALE', 'best, audrey'),
       (8, 'Mathieu', 'Kassovitz', '1967-08-03', 'MALE', ''),
       (8, 'Jamel', 'Debbouze', '1975-06-18', 'MALE', 'best'),
       (8, 'Dominique', 'Pinon', '1955-03-04', 'MALE', ''),
       (8, 'Gaspard', 'Ulliel', '1984-11-25', 'MALE', ''),
       (8, 'Guillaume', 'Canet', '1973-04-10', 'MALE', ''),
       (8, 'Gad', 'Elmaleh', '1971-04-19', 'MALE', ''),
       (18, 'Hugh', 'Laurie', '1959-06-11', 'MALE', 'best, house'),
       (18, 'Stephen', 'Fry', '1957-08-24', 'MALE', 'best'),
       (19, 'Keanu', 'Reeves', '1964-09-02', 'MALE', ''),
       (19, 'Julia', 'Roberts', '1967-10-28', 'FEMALE', 'best, julia'),
       (19, 'George', 'Clooney', '1967-10-28', 'MALE', 'best, clooney'),
       (19, 'Brad', 'Pitt', '1963-12-18', 'MALE', ''),
       (19, 'Susan', 'Sarandon', '1946-10-04', 'FEMALE', ''),
       (19, 'Richard', 'Gere', '1949-08-31', 'MALE', ''),
       (19, 'Salma', 'Hayek', '1966-09-02', 'FEMALE', '');


insert into film_actor(film_id, actor_id)
values (1, 1),
       (1, 2),
       (1, 3),
       (1, 4),
       (2, 1),
       (2, 4),
       (2, 5),
       (3, 1),
       (3, 6),
       (4, 1),
       (4, 7),
       (5, 8),
       (6, 8),
       (6, 9),
       (7, 8),
       (7, 10),
       (8, 8),
       (9, 11),
       (9, 12),
       (9, 13),
       (10, 11),
       (10, 14),
       (11, 11),
       (11, 15),
       (12, 12),
       (12, 16);
