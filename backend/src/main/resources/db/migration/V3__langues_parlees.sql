-- Langues parlées : une langue commune est obligatoire pour être proposés l'un à l'autre
create table utilisateur_langue (
    utilisateur_id bigint     not null references utilisateur (id) on delete cascade,
    langue         varchar(2) not null,
    primary key (utilisateur_id, langue)
);

-- comptes existants : le français, langue de l'interface d'origine
insert into utilisateur_langue (utilisateur_id, langue) select id, 'FR' from utilisateur;
