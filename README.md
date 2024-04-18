Класи та їх атрибути:

Клас Книга:
назва (String): Назва книги. Анотація @NotEmpty вказує, що значення не повинно бути порожнім.
рік_публікації (Date): Рік публікації книги. Анотація @NotNull показує, що значення не повинно бути null. Анотація @PastOrPresent потребує, щоб дата була або в минулому, або сьогоднішньою чи раніше. Анотація @JsonFormat визначає формат дати.
жанри (Genre): Жанр книги. Анотація @NotNull показує, що значення не повинно бути null.
автор (Author): Автор книги. Анотація @NotNull показує, що значення не повинно бути null.
опис (String): Опис книги. Анотація @Size обмежує довжину рядка.

Клас Автор:
ім'я (String): Ім'я автора. Анотація @Pattern перевіряє, відповідає чи не відповідає ім'я певному шаблону. Анотація @Size визначає мінімальну довжину імені, а @NotEmpty вказує, що значення не повинно бути порожнім.
вік (int): Вік автора. Анотація @NotNull показує, що значення не повинно бути null.
національність (String): Національність автора. Анотація @Pattern перевіряє відповідність шаблону, а @NotEmpty вказує, що значення не повинно бути порожнім.
день_народження (Date): Дата народження автора. Анотація @NotNull показує, що значення не повинно бути null, @Past потребує, щоб дата була в минулому, а @JsonFormat визначає формат дати.

Перелік Жанр:
Роман: Роман
Фентезі: Фентезі
Містика: Містика
Наукова_фантастика: Наукова фантастика
Жахи: Жахи
Трилер: Трилер
Наукова_література: Наукова література
Ці класи визначають структуру даних для книги та її автора, включаючи їх атрибути та обмеження, які повинні бути дотримані при роботі з ними.


Зразок вхiдних файлiв 
https://github.com/tatarynovdima/first-task-java/blob/master/src/main/resources/example.json

Зразок вихiдних файлiв 
https://github.com/tatarynovdima/first-task-java/blob/master/src/main/resources/statistics_by_genres.xml

Вимірювання часу парсингу для різної кількості потоків
Тестовий набір був попередньо створений за допомогою ChatGPT із параметрами BOOK_COUNT (загальна кількість сутностей) 10000, та FILE_COUNT (кількість файлів) 100.

Вимірювання виконане за допомогою тесту

Number of threads 1, time - 11036 Millisecond
Number of threads 2, time - 5567 Millisecond
Number of threads 4, time - 3323 Millisecond
Number of threads 8, time - 2748 Millisecond


За наданими статистикою ми бачимо, що час виконання програми скорочується зі збільшенням кількості потоків, які використовуються для виконання завдання. Це свідчить про паралельну обробку даних і ефективне використання багатопотоковості для прискорення роботи програми.

Основні спостереження:

При одному потоці виконання займає 11036 мілісекунд.
При збільшенні кількості потоків до двох, час виконання скорочується до 5567 мілісекунд.
Подальше збільшення кількості потоків до чотирьох і восьми подальше скорочує час виконання до 3323 і 2748 мілісекунд відповідно.
Це показує, що паралельна обробка допомагає суттєво прискорити виконання програми, особливо при обробці великих обсягів даних. Однак слід пам'ятати, що ефективність паралельної обробки обмежена не лише кількістю доступних ядер процесора, але і характером завдання, структурою даних та іншими факторами.

Час парсингу значень однієї властивості сутності для такого ж набору даних за допомогою Jackson Streaming API (тест com.streamlined.dataprocessor.parser.MultithreadParsePerformanceTest.measureStreamingParseTime) значно менший, що свідчить про перевагу даного методу. Крім того, він потребує лише фіксований обсяг пам'яті буферу для збереження даних перед опрацюванням, без виділення пам'яті для створення повної колекції сутностей.

Number of threads 1, time - 6240 Millisecond Streaming API
Number of threads 2, time - 4203 Millisecond Streaming API
Number of threads 4, time - 2461 Millisecond Streaming API
Number of threads 8, time - 1247 Millisecond Streaming API


Запуск програми
Чтобы запустить программу нужно будет запустить jar з параметрами java -jar first-task-java.jar путь к папке где есть файлы нейм атрiбуту 


Структура проекта ![image](https://github.com/tatarynovdima/first-task-java/assets/113349562/51219694-8d19-4a49-9c18-001d58df3a40)
