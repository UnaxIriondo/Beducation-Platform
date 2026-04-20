import random

# ============================================================
# Generador de datos de prueba para Beducation
# IDs obtenidos de la DB real:
#   education_types: 1 (Grado Superior), 2 (Grado Universitario), 3 (Bootcamp)
#   keywords: 1-7 (Java, Spring Boot, Vue.js, Python, SEO, Social Media, Analista de datos)
# ============================================================

BCRYPT_PASSWORD = "$2a$10$0zX7fHn2tU1H.0Z5n8E1w.X2Z8Z.2nN5w9Y2Yp2Wj5eA2FROX6"  # "123456"
CITIES = ["Madrid", "Barcelona", "Bilbao", "Valencia", "Sevilla", "Zaragoza", "Malaga", "Murcia"]
COUNTRIES = ["Espana", "Francia", "Alemania", "Reino Unido", "Italia", "Portugal"]
COMPANY_NAMES = [
    "TechCorp", "InnovaSolutions", "GlobalData", "NextGen Systems", "CreativeStudio",
    "MarketingPro", "FinTech Solutions", "DesignHub", "CloudNet", "EcoEnergy",
    "AgriTech", "HealthPlus", "EduSmart", "LogiCorp", "MediaGroup",
    "AutoDrive", "ConstructCo", "FoodServices", "RetailMax", "CyberSec"
]
SCHOOL_NAMES = [
    "Escuela Tecnica Superior", "Instituto Tecnologico", "Centro de Formacion Profesional",
    "Academia de Negocios", "Campus Digital", "Instituto de Diseno",
    "Centro FP Avanzado", "Escuela de Marketing", "Instituto Financiero", "Campus Tech"
]
STUDENT_FIRST_NAMES = [
    "Juan", "Maria", "David", "Laura", "Carlos", "Ana", "Miguel", "Carmen",
    "Antonio", "Lucia", "Javier", "Marta", "Jose", "Elena", "Daniel",
    "Paula", "Francisco", "Sara", "Alejandro", "Raquel"
]
STUDENT_LAST_NAMES = [
    "Garcia", "Martinez", "Lopez", "Sanchez", "Perez", "Gomez", "Martin",
    "Rodriguez", "Fernandez", "Gonzalez", "Ruiz", "Diaz", "Alvarez",
    "Moreno", "Munoz", "Romero", "Alonso", "Gutierrez", "Navarro", "Torres"
]
OPPORTUNITY_TITLES = [
    "Desarrollador Junior", "Analista de Datos", "Disenador Grafico",
    "Especialista en Marketing", "Consultor Financiero", "Administrador de Sistemas",
    "Ingeniero DevOps", "Asistente RRHH", "Project Manager", "Desarrollador Frontend",
    "Desarrollador Backend", "Community Manager", "Auditor", "Disenador UX"
]

# IDs reales en la DB
EDUCATION_TYPE_IDS = [1, 2, 3]
KEYWORD_IDS = list(range(1, 8))  # 1-7

sql_lines = []
sql_lines.append("-- ============================================================")
sql_lines.append("-- V6__test_data.sql - Datos de prueba masivos")
sql_lines.append("-- ============================================================")
sql_lines.append("")
sql_lines.append("-- Limpieza de datos de prueba previos")
sql_lines.append("DELETE FROM interviews;")
sql_lines.append("DELETE FROM applications;")
sql_lines.append("DELETE FROM opportunity_keywords;")
sql_lines.append("DELETE FROM student_keywords;")
sql_lines.append("DELETE FROM student_country_preferences;")
sql_lines.append("DELETE FROM opportunities;")
sql_lines.append("DELETE FROM students;")
sql_lines.append("DELETE FROM schools;")
sql_lines.append("DELETE FROM companies;")
sql_lines.append("DELETE FROM users WHERE email LIKE '%@test.com';")
sql_lines.append("")

# ---- ESCUELAS (10) ----
sql_lines.append("-- Inserción de 10 escuelas de prueba")
for i in range(10):
    email = f"school{i+1}@test.com"
    name = SCHOOL_NAMES[i]
    city = random.choice(CITIES)
    sql_lines.append(
        f"INSERT INTO users (email, role, status, password) VALUES ('{email}', 'SCHOOL', 'ACTIVE', '{BCRYPT_PASSWORD}');"
    )
    sql_lines.append(
        f"INSERT INTO schools (user_id, name, country, city, status) "
        f"SELECT id, '{name}', 'Espana', '{city}', 'APPROVED' FROM users WHERE email='{email}';"
    )
sql_lines.append("")

# ---- EMPRESAS (20) ----
sql_lines.append("-- Inserción de 20 empresas de prueba")
for i in range(20):
    email = f"company{i+1}@test.com"
    name = COMPANY_NAMES[i % len(COMPANY_NAMES)]
    city = random.choice(CITIES)
    status = random.choice(["APPROVED", "APPROVED", "APPROVED", "PENDING"])
    sql_lines.append(
        f"INSERT INTO users (email, role, status, password) VALUES ('{email}', 'COMPANY', 'ACTIVE', '{BCRYPT_PASSWORD}');"
    )
    sql_lines.append(
        f"INSERT INTO companies (user_id, name, country, city, status) "
        f"SELECT id, '{name}', 'Espana', '{city}', '{status}' FROM users WHERE email='{email}';"
    )
sql_lines.append("")

# ---- ESTUDIANTES (100) ----
sql_lines.append("-- Inserción de 100 estudiantes de prueba")
school_pool = [f"school{i+1}@test.com" for i in range(10)]
student_emails = []
for i in range(100):
    email = f"student{i+1}@test.com"
    school_email = school_pool[i % 10]
    first = random.choice(STUDENT_FIRST_NAMES)
    last = random.choice(STUDENT_LAST_NAMES)
    edu_type = random.choice(EDUCATION_TYPE_IDS)
    student_emails.append(email)

    sql_lines.append(
        f"INSERT INTO users (email, role, status, password) VALUES ('{email}', 'STUDENT', 'ACTIVE', '{BCRYPT_PASSWORD}');"
    )
    sql_lines.append(
        f"INSERT INTO students (user_id, school_id, first_name, last_name, invitation_email, education_type_id, profile_complete, onboarding_complete) "
        f"SELECT u.id, s.id, '{first}', '{last}', '{email}', {edu_type}, 1, 1 "
        f"FROM users u JOIN schools s ON s.user_id=(SELECT id FROM users WHERE email='{school_email}') "
        f"WHERE u.email='{email}';"
    )

    # Preferencias de país (1-2)
    chosen_countries = random.sample(COUNTRIES, random.randint(1, 2))
    for cc in chosen_countries:
        sql_lines.append(
            f"INSERT INTO student_country_preferences (student_id, country_code) "
            f"SELECT id, '{cc}' FROM students WHERE invitation_email='{email}';"
        )

    # Keywords (2-4)
    chosen_kws = random.sample(KEYWORD_IDS, random.randint(2, 4))
    for kw in chosen_kws:
        sql_lines.append(
            f"INSERT INTO student_keywords (student_id, keyword_id) "
            f"SELECT id, {kw} FROM students WHERE invitation_email='{email}';"
        )
sql_lines.append("")

# ---- OPORTUNIDADES (80) ----
sql_lines.append("-- Inserción de 80 oportunidades de prueba")
company_pool = [f"company{i+1}@test.com" for i in range(20)]
opp_refs = []  # (company_email, title_unique)
for i in range(80):
    company_email = company_pool[i % 20]
    base_title = random.choice(OPPORTUNITY_TITLES)
    title_unique = f"{base_title} Ref{i+1}"
    edu_type = random.choice(EDUCATION_TYPE_IDS)
    spots = random.randint(1, 5)
    status = random.choice(["APPROVED", "APPROVED", "APPROVED", "DRAFT", "PENDING_REVIEW"])
    country = random.choice(COUNTRIES)
    city = random.choice(CITIES)
    desc = f"Buscamos un perfil con experiencia en {base_title}. Posicion {i+1}."

    sql_lines.append(
        f"INSERT INTO opportunities (company_id, title, description, country, city, spots, spots_available, education_type_id, status) "
        f"SELECT c.id, '{title_unique}', '{desc}', '{country}', '{city}', {spots}, {spots}, {edu_type}, '{status}' "
        f"FROM companies c JOIN users u ON c.user_id=u.id WHERE u.email='{company_email}';"
    )
    # Keywords de la oportunidad (2-3)
    chosen_kws = random.sample(KEYWORD_IDS, random.randint(2, 3))
    for kw in chosen_kws:
        sql_lines.append(
            f"INSERT INTO opportunity_keywords (opportunity_id, keyword_id) "
            f"SELECT TOP 1 id, {kw} FROM opportunities WHERE title='{title_unique}';"
        )
    opp_refs.append((company_email, title_unique, status))
sql_lines.append("")

# ---- APLICACIONES (200) ----
sql_lines.append("-- Inserción de 200 aplicaciones de prueba")
app_pairs_seen = set()
apps_inserted = 0
attempts = 0
while apps_inserted < 200 and attempts < 2000:
    attempts += 1
    stud_email = random.choice(student_emails)
    opp_idx = random.randint(0, len(opp_refs) - 1)
    _, opp_title, opp_status = opp_refs[opp_idx]

    # Solo insertar en oportunidades aprobadas y evitar duplicados estudiante-oferta
    pair = (stud_email, opp_title)
    if opp_status != "APPROVED" or pair in app_pairs_seen:
        continue
    app_pairs_seen.add(pair)

    status = random.choice(["APPLIED", "INTERESTED", "OFFERED", "ADMIN_APPROVED", "CONFIRMED"])
    stage = random.randint(1, 4)
    sql_lines.append(
        f"INSERT INTO applications (student_id, opportunity_id, stage, status) "
        f"SELECT st.id, opp.id, {stage}, '{status}' "
        f"FROM students st JOIN opportunities opp ON opp.title='{opp_title}' "
        f"WHERE st.invitation_email='{stud_email}';"
    )
    apps_inserted += 1

sql_lines.append("")
sql_lines.append(f"-- Total aplicaciones: {apps_inserted}")
sql_lines.append("-- Fin de V6__test_data.sql")

output_path = r"c:\Users\Onddo\Desktop\BEDUCATION\Beducation-Platform\beducation-backend\src\main\resources\db\migration\V6__test_data.sql"
with open(output_path, "w", encoding="utf-8") as f:
    f.write("\n".join(sql_lines))

print(f"OK - Generado V6__test_data.sql con {apps_inserted} aplicaciones.")
