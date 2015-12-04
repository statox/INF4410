#!/bin/bash

# reponses au questions de mysql-server-5.5 et de phpmyadmin en utilisant les outils fournis dans le paquage debconf-utils

debconf-set-selections <<< 'mysql-server mysql-server/root_password password root'
debconf-set-selections <<< 'mysql-server mysql-server/root_password_again password root'


debconf-set-selections <<< 'phpmyadmin phpmyadmin/dbconfig-install boolean true'                
debconf-set-selections <<< 'phpmyadmin phpmyadmin/app-password-confirm password root'   
debconf-set-selections <<< 'phpmyadmin phpmyadmin/mysql/admin-pass password root'  
debconf-set-selections <<< 'phpmyadmin phpmyadmin/mysql/app-pass password root'      
debconf-set-selections <<< 'phpmyadmin phpmyadmin/setup-password password root'    
debconf-set-selections <<< 'phpmyadmin phpmyadmin/reconfigure-webserver multiselect apache2'    

# installation de mysql-server, mysql-client, apache2,  php5, libapache2-mod-php5, php5-mysql, phpmyadmin. Dans le meme ordre

apt-get -y install mysql-server mysql-client apache2 php5 libapache2-mod-php5 php5-mysql phpmyadmin

#Ajout du fichier de configuration apache
echo "Include /etc/phpmyadmin/apache.conf" | sudo tee --append /etc/apache2/apache2.conf
service apache2 restart
