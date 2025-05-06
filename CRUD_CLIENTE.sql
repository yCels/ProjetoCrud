

use vendas ;

create table cliente(
id int auto_increment primary key,
nome varchar(50),
cpf varchar (30) unique,
email varchar(60),
telefone varchar(30),
endereço varchar(50),
data_nascimento date);

select * from cliente;


