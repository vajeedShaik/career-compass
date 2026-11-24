CREATE TABLE learning_resources (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    skill           VARCHAR(100) NOT NULL,
    title           VARCHAR(255) NOT NULL,
    url             VARCHAR(500) NOT NULL,
    resource_type   VARCHAR(50)  NOT NULL DEFAULT 'documentation'
);

CREATE INDEX idx_learning_resources_skill ON learning_resources (LOWER(skill));

-- Curated, real resources for common skill gaps this project is likely to
-- surface. Kept small and hand-picked rather than scraped, so the RAG step
-- can only ever recommend resources that actually exist.

INSERT INTO learning_resources (skill, title, url, resource_type) VALUES
('Spring Boot', 'Spring Boot Official Reference Documentation', 'https://docs.spring.io/spring-boot/reference/', 'documentation'),
('Spring Boot', 'Spring Boot Quickstart Guide', 'https://spring.io/guides/gs/spring-boot', 'tutorial'),
('REST APIs', 'Building a RESTful Web Service (Spring Guide)', 'https://spring.io/guides/gs/rest-service', 'tutorial'),
('REST APIs', 'REST API Tutorial', 'https://restfulapi.net/', 'documentation'),
('PostgreSQL', 'PostgreSQL Official Tutorial', 'https://www.postgresql.org/docs/current/tutorial.html', 'documentation'),
('PostgreSQL', 'PostgreSQL Exercises (interactive)', 'https://pgexercises.com/', 'practice'),
('Hibernate/JPA', 'Spring Data JPA Reference Documentation', 'https://docs.spring.io/spring-data/jpa/reference/', 'documentation'),
('Hibernate/JPA', 'Baeldung: Guide to JPA with Spring', 'https://www.baeldung.com/the-persistence-layer-with-spring-and-jpa', 'tutorial'),
('Docker', 'Docker Official Get Started Guide', 'https://docs.docker.com/get-started/', 'tutorial'),
('Docker', 'Docker Curriculum (free, hands-on)', 'https://docker-curriculum.com/', 'tutorial'),
('Kubernetes', 'Kubernetes Official Basics Tutorial', 'https://kubernetes.io/docs/tutorials/kubernetes-basics/', 'tutorial'),
('Kafka', 'Apache Kafka Official Documentation', 'https://kafka.apache.org/documentation/', 'documentation'),
('Kafka', 'Spring for Apache Kafka Reference', 'https://docs.spring.io/spring-kafka/reference/', 'documentation'),
('JWT', 'JWT Introduction (jwt.io)', 'https://jwt.io/introduction', 'documentation'),
('Git', 'Pro Git Book (free, official)', 'https://git-scm.com/book/en/v2', 'documentation'),
('Java', 'Oracle Java Tutorials', 'https://docs.oracle.com/javase/tutorial/', 'documentation'),
('Python', 'Official Python Tutorial', 'https://docs.python.org/3/tutorial/', 'documentation'),
('SQL', 'Mode SQL Tutorial (free, interactive)', 'https://mode.com/sql-tutorial/', 'tutorial'),
('AWS', 'AWS Skill Builder Free Digital Courses', 'https://skillbuilder.aws/', 'tutorial'),
('React', 'React Official Documentation', 'https://react.dev/learn', 'documentation'),
('TensorFlow', 'TensorFlow Official Tutorials', 'https://www.tensorflow.org/tutorials', 'documentation'),
('Microservices', 'Spring Cloud Reference Documentation', 'https://docs.spring.io/spring-cloud/docs/current/reference/html/', 'documentation');
