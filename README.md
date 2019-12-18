В этом проекте изучаются следующие вопросы:
1) Можете уточнить как именно включить PeerClassLoading  для локальной разработки? Как это меняется вне файла конфига?
2) Дело в том, что кешы могут не содержать таблицы (какой-нибудь владелец данных их не создал), 
это значит что для запроса надо эти таблицы создать. Имеем ли мы права на создание таблиц и не будет ли это влиять на кого либо как либо?

Тест состоит из следующих шагов:
1. запустить серевер: ServerNodeSpringStartup#main
2. создать кэш без таблицы: ClientNodeSpringStartup#createCacheWithoutTable
3. получить данные через итератор и SQL: ClientNodeSpringStartup#gettingValuesFromCache
Через SQL ожидаемо получаем ошибку.
4. создаем таблицу для кэша: ClientNodeSpringStartup#createTableForCache
5. повторно вызываем получение данных: ClientNodeSpringStartup#gettingValuesFromCache
теперь без ошибок
