# 🚀 Servinet — Sistema de Gestión de Servicios

**Una aplicación de escritorio desarrollada en JavaFX para gestionar clientes, técnicos, instalaciones y operaciones de servicio desde una interfaz moderna e intuitiva.**

<p align="center">
  <img src="https://skillicons.dev/icons?i=java,maven" alt="Java y Maven" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=openjdk" alt="Java 21" />
  <img src="https://img.shields.io/badge/JavaFX-21-blue?style=for-the-badge" alt="JavaFX 21" />
  <img src="https://img.shields.io/badge/SQL%20Server-Database-red?style=for-the-badge&logo=microsoftsqlserver" alt="SQL Server" />
  <img src="https://img.shields.io/badge/Architecture-Clean%20Architecture-6C63FF?style=for-the-badge" alt="Clean Architecture" />
</p>

---

## 📸 Vista previa

<p align="center">
  <img src="https://github.com/user-attachments/assets/d76d1c62-749a-4826-87ee-8c572ace1239" alt="Interfaz de Servinet" width="100%" />
</p>

<p align="center">
  <b>Una interfaz diseñada para facilitar la administración y el control de las operaciones diarias.</b>
</p>

---

## ✨ Características principales

- 👥 **Gestión de clientes:** administración de la información y los datos de contacto.
- 🛠️ **Gestión de técnicos:** organización y administración del personal técnico.
- 📡 **Control de instalaciones:** seguimiento de órdenes y trabajos técnicos.
- 💳 **Gestión de pagos:** registro y administración de operaciones de pago.
- 🔐 **Seguridad:** almacenamiento de contraseñas mediante Argon2 y gestión de usuarios.
- ⚙️ **Configuración flexible:** parámetros de la aplicación gestionados mediante YAML.
- 🧱 **Arquitectura organizada:** separación de responsabilidades siguiendo principios de Clean Architecture.

## 🧰 Tecnologías utilizadas

| Tecnología | Uso |
|---|---|
| ☕ Java 21 | Lenguaje principal |
| 🎨 JavaFX 21 | Interfaz gráfica de escritorio |
| 🧩 FXML | Estructuración de las vistas |
| 🖌️ CSS | Estilos visuales |
| 📦 Maven | Gestión de dependencias y construcción |
| 🗄️ Microsoft SQL Server | Persistencia de datos |
| 🔌 JDBC | Conectividad con la base de datos |
| 🎯 Ikonli | Iconografía de la interfaz |
| 🔒 Argon2 | Hash seguro de contraseñas |
| ⚙️ YAML | Configuración de la aplicación |
| 🏛️ Clean Architecture | Organización y separación de responsabilidades |

## 📋 Requisitos previos

Antes de ejecutar Servinet, asegúrate de contar con:

- **Java JDK 21**
- **Maven**
- **Microsoft SQL Server**
- **SQL Server Management Studio (SSMS)** — recomendado para administrar la base de datos.

Comprueba tu entorno desde la terminal:

```bash
java -version
mvn -version
```

## ▶️ Ejecución del proyecto

1. Clona el repositorio y accede a su directorio:

   ```bash
   git clone <URL_DEL_REPOSITORIO>
   cd Servinet
   ```

2. Configura la conexión a SQL Server en local o establece un servidor en la nube para su correcto funcionamiento.
