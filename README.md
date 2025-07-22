# Projeto Recorder - Sistema de Agendamento e Galeria

Este projeto foi migrado de MySQL para PostgreSQL e inclui funcionalidades de upload de imagens para o Supabase.

## 🚀 Tecnologias Utilizadas

### Backend
- Java 17
- Spring Boot 3.5.0
- Spring Security
- Spring Data JPA
- PostgreSQL
- JWT para autenticação
- Supabase Storage para upload de arquivos

### Frontend
- HTML5
- CSS3
- JavaScript (Vanilla)
- Fetch API para comunicação com backend

## 📁 Estrutura do Projeto

```
projeto-migrado/
├── backend/                 # Aplicação Spring Boot
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/
│   │   │   └── resources/
│   │   └── test/
│   └── pom.xml
├── frontend/               # Aplicação frontend
│   ├── public/
│   │   ├── css/
│   │   ├── js/
│   │   └── images/
│   └── views/
├── docker/                 # Configurações Docker
│   ├── Dockerfile
│   └── docker-compose.yml
├── docs/                   # Documentação
│   ├── supabase-setup.md
│   ├── render-deployment.md
│   └── migration-guide.md
└── README.md
```

## 🛠️ Configuração Local

### Pré-requisitos
- Java 17+
- Maven 3.6+
- PostgreSQL 12+
- Node.js (opcional, para servir frontend)

### 1. Configurar Banco de Dados
```sql
CREATE DATABASE "db-gravAcao";
```

### 2. Configurar Variáveis de Ambiente
Copie o arquivo `.env.example` para `.env` e configure suas variáveis:
```bash
cp .env.example .env
```

### 3. Executar com Docker (Recomendado)
```bash
cd docker
docker-compose up -d
```

### 4. Executar Manualmente

#### Backend
```bash
cd backend
mvn spring-boot:run
```

#### Frontend
Sirva os arquivos estáticos usando um servidor web simples:
```bash
cd frontend
python3 -m http.server 3000
```

## 🌐 Endpoints da API

### Autenticação
- `POST /api/auth/login` - Login
- `POST /api/auth/register` - Registro

### Galeria
- `GET /api/galeria` - Listar todas as mídias
- `GET /api/galeria/{id}` - Buscar mídia por ID
- `POST /api/galeria/upload` - Upload de arquivo
- `DELETE /api/galeria/{id}` - Deletar mídia
- `GET /api/galeria/tipo/{tipo}` - Filtrar por tipo (FOTO/VIDEO)

### Agendamentos
- `GET /api/agendamentos` - Listar agendamentos
- `POST /api/agendamentos` - Criar agendamento
- `PUT /api/agendamentos/{id}` - Atualizar agendamento
- `DELETE /api/agendamentos/{id}` - Deletar agendamento

## 📚 Documentação Adicional

- [Configuração do Supabase](docs/supabase-setup.md)
- [Deploy no Render](docs/render-deployment.md)
- [Guia de Migração](docs/migration-guide.md)

## 🔧 Principais Mudanças na Migração

1. **Banco de Dados**: MySQL → PostgreSQL
2. **Driver JDBC**: `mysql-connector-j` → `postgresql`
3. **Dialect Hibernate**: `MySQLDialect` → `PostgreSQLDialect`
4. **Upload de Arquivos**: Implementação com Supabase Storage
5. **Galeria**: Sistema completo de upload e visualização

## 🚀 Deploy

### Render (Backend)
Veja o guia completo em [docs/render-deployment.md](docs/render-deployment.md)

### Supabase (Banco + Storage)
Veja o guia completo em [docs/supabase-setup.md](docs/supabase-setup.md)

## 📝 Licença

Este projeto está sob a licença MIT.

