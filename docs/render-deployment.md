# Guia Completo de Deploy no Render

Este guia detalha como fazer o deploy do backend Spring Boot e frontend do projeto Recorder na plataforma Render.

## 📋 Índice

1. [Preparação do Projeto](#preparação-do-projeto)
2. [Deploy do Backend (Docker)](#deploy-do-backend-docker)
3. [Deploy do Frontend (Static Site)](#deploy-do-frontend-static-site)
4. [Configuração de Variáveis de Ambiente](#configuração-de-variáveis-de-ambiente)
5. [Configuração de Domínio Customizado](#configuração-de-domínio-customizado)
6. [Monitoramento e Logs](#monitoramento-e-logs)
7. [Troubleshooting](#troubleshooting)
8. [Otimizações de Performance](#otimizações-de-performance)

## 🚀 Preparação do Projeto

### Passo 1: Criar Conta no Render

1. Acesse [https://render.com](https://render.com)
2. Clique em "Get Started"
3. Faça login com GitHub, GitLab ou Google
4. Conecte sua conta do GitHub para acessar os repositórios

### Passo 2: Preparar Repositório Git

1. Crie um repositório no GitHub para o projeto
2. Faça upload dos arquivos do projeto:

```bash
git init
git add .
git commit -m "Initial commit - Projeto Recorder migrado para PostgreSQL"
git branch -M main
git remote add origin https://github.com/seu-usuario/recorder-app.git
git push -u origin main
```

### Passo 3: Estrutura de Arquivos Necessária

Certifique-se de que seu repositório tenha esta estrutura:

```
projeto-migrado/
├── backend/
│   ├── src/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── public/
│   └── views/
├── docker/
│   ├── Dockerfile
│   └── docker-compose.yml
└── render.yaml (opcional)
```

## 🐳 Deploy do Backend (Docker)

### Passo 4: Criar Dockerfile Otimizado

Crie um `Dockerfile` na raiz do projeto (ou use o existente em `/docker/`):

```dockerfile
# Multi-stage build para otimizar tamanho da imagem
FROM maven:3.8.6-openjdk-17-slim AS build

# Definir diretório de trabalho
WORKDIR /app

# Copiar apenas pom.xml primeiro (para cache de dependências)
COPY backend/pom.xml .

# Baixar dependências (será cacheado se pom.xml não mudar)
RUN mvn dependency:go-offline -B

# Copiar código fonte
COPY backend/src ./src

# Compilar aplicação
RUN mvn clean package -DskipTests

# Estágio final - imagem de produção
FROM openjdk:17-jdk-slim

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Criar usuário não-root para segurança
RUN addgroup --system spring && adduser --system spring --ingroup spring
USER spring:spring

# Definir diretório de trabalho
WORKDIR /app

# Copiar JAR da etapa de build
COPY --from=build /app/target/*.jar app.jar

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/actuator/health || exit 1

# Comando para executar aplicação
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Passo 5: Configurar Web Service no Render

1. No dashboard do Render, clique em "New +"
2. Selecione "Web Service"
3. Conecte seu repositório GitHub
4. Configure o serviço:

**Configurações Básicas:**
- **Name**: `recorder-backend`
- **Environment**: `Docker`
- **Region**: Escolha a região mais próxima
- **Branch**: `main`
- **Root Directory**: deixe vazio (ou `./` se necessário)

**Configurações de Build:**
- **Dockerfile Path**: `Dockerfile` (ou `docker/Dockerfile`)

**Configurações de Deploy:**
- **Instance Type**: `Free` (para começar)
- **Auto-Deploy**: ✅ Ativado

### Passo 6: Configurar Variáveis de Ambiente do Backend

Na seção "Environment Variables", adicione:

```bash
# Configurações do Banco de Dados
SPRING_DATASOURCE_URL=postgresql://postgres:[SENHA]@db.[PROJETO].supabase.co:5432/postgres
SPRING_DATASOURCE_USERNAME=postgres
SPRING_DATASOURCE_PASSWORD=[SUA-SENHA-SUPABASE]
SPRING_DATASOURCE_DRIVER_CLASS_NAME=org.postgresql.Driver

# Configurações JPA/Hibernate
SPRING_JPA_HIBERNATE_DDL_AUTO=update
SPRING_JPA_SHOW_SQL=false
SPRING_JPA_PROPERTIES_HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect

# Configurações do Supabase
SUPABASE_URL=https://[SEU-PROJETO].supabase.co
SUPABASE_ANON_KEY=[SUA-CHAVE-ANON]
SUPABASE_BUCKET=galeria

# Configurações de Upload
SPRING_SERVLET_MULTIPART_MAX_FILE_SIZE=10MB
SPRING_SERVLET_MULTIPART_MAX_REQUEST_SIZE=10MB

# Configurações JWT
JWT_SECRET=Y9cmO4GzR47fj1+zatJrxbboch8mLX9SGURxkUWy3uhqPdX6l+ouMJTd9OAOvBfu7cmoVFRPalqUpElVvDsKJg==
JWT_EXPIRATION=86400000

# Configurações de Produção
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
```

### Passo 7: Adicionar Health Check

Adicione ao `application.properties`:

```properties
# Actuator para health checks
management.endpoints.web.exposure.include=health,info
management.endpoint.health.show-details=always
management.health.db.enabled=true
```

E adicione a dependência no `pom.xml`:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-actuator</artifactId>
</dependency>
```

## 🌐 Deploy do Frontend (Static Site)

### Passo 8: Preparar Frontend para Produção

1. Atualize as URLs da API no frontend para usar a URL do backend no Render
2. No arquivo `frontend/public/js/config.js` (crie se não existir):

```javascript
// Configuração da API baseada no ambiente
const API_CONFIG = {
    BASE_URL: window.location.hostname === 'localhost' 
        ? 'http://localhost:8080/api'
        : 'https://recorder-backend.onrender.com/api'
};

// Exportar para uso global
window.API_BASE_URL = API_CONFIG.BASE_URL;
```

3. Atualize todos os arquivos JS para usar `window.API_BASE_URL` em vez de URLs hardcoded

### Passo 9: Criar Static Site no Render

1. No dashboard do Render, clique em "New +"
2. Selecione "Static Site"
3. Conecte o mesmo repositório
4. Configure o site:

**Configurações Básicas:**
- **Name**: `recorder-frontend`
- **Branch**: `main`
- **Root Directory**: `frontend`
- **Build Command**: deixe vazio (arquivos já estão prontos)
- **Publish Directory**: `public`

**Configurações Avançadas:**
- **Auto-Deploy**: ✅ Ativado

### Passo 10: Configurar Redirects e Headers

Crie um arquivo `frontend/public/_redirects`:

```
# Redirect all routes to index.html for SPA behavior
/*    /views/inicial.html   200

# API proxy (opcional, se quiser evitar CORS)
/api/*  https://recorder-backend.onrender.com/api/:splat  200
```

Crie um arquivo `frontend/public/_headers`:

```
/*
  X-Frame-Options: DENY
  X-XSS-Protection: 1; mode=block
  X-Content-Type-Options: nosniff
  Referrer-Policy: strict-origin-when-cross-origin
  Content-Security-Policy: default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; connect-src 'self' https://recorder-backend.onrender.com https://*.supabase.co;
```

## 🔧 Configuração de Variáveis de Ambiente

### Passo 11: Configurar CORS no Backend

Adicione configuração CORS para permitir acesso do frontend:

```java
@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "http://localhost:3000",
                    "https://recorder-frontend.onrender.com",
                    "https://seu-dominio-customizado.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
```

### Passo 12: Configurar Environment Variables Dinâmicas

Para facilitar mudanças, use variáveis de ambiente no frontend também:

```javascript
// frontend/public/js/config.js
const API_CONFIG = {
    BASE_URL: '${API_BASE_URL}' || 'https://recorder-backend.onrender.com/api'
};
```

E configure no Render Static Site:
- **Environment Variables**: `API_BASE_URL=https://recorder-backend.onrender.com/api`

## 🌍 Configuração de Domínio Customizado

### Passo 13: Configurar Domínio Personalizado

**Para o Backend:**
1. Vá para o serviço backend no Render
2. Clique em "Settings" → "Custom Domains"
3. Adicione seu domínio (ex: `api.seudominio.com`)
4. Configure DNS CNAME apontando para `recorder-backend.onrender.com`

**Para o Frontend:**
1. Vá para o static site no Render
2. Clique em "Settings" → "Custom Domains"
3. Adicione seu domínio (ex: `www.seudominio.com`)
4. Configure DNS CNAME apontando para `recorder-frontend.onrender.com`

### Passo 14: Configurar SSL/TLS

O Render fornece SSL automático via Let's Encrypt:
1. Aguarde a propagação DNS (pode levar até 24h)
2. O SSL será configurado automaticamente
3. Verifique se o certificado está ativo na seção "Custom Domains"

## 📊 Monitoramento e Logs

### Passo 15: Configurar Logging

**Backend - application.properties:**
```properties
# Configurações de log para produção
logging.level.root=INFO
logging.level.com.recorder=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss} - %msg%n
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n

# Log de SQL apenas em desenvolvimento
logging.level.org.hibernate.SQL=WARN
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=WARN
```

### Passo 16: Monitorar Performance

1. **Render Dashboard**: Monitore CPU, memória e requests
2. **Logs**: Acesse logs em tempo real via dashboard
3. **Metrics**: Configure alertas para downtime
4. **Health Checks**: Monitore endpoint `/actuator/health`

### Passo 17: Configurar Alertas

1. Vá para "Settings" → "Alerts" em cada serviço
2. Configure alertas para:
   - Deploy failures
   - Service downtime
   - High resource usage
   - Failed health checks

## 🔧 Troubleshooting

### Problemas Comuns

**1. Build Failure:**
```bash
# Verificar logs de build
# Comum: dependências não encontradas, versão Java incorreta

# Solução: Verificar pom.xml e Dockerfile
```

**2. Connection Timeout:**
```bash
# Problema: Timeout conectando com Supabase
# Solução: Verificar variáveis de ambiente e firewall
```

**3. CORS Error:**
```bash
# Problema: Frontend não consegue acessar API
# Solução: Configurar CORS corretamente no backend
```

**4. File Upload Error:**
```bash
# Problema: Upload para Supabase falha
# Solução: Verificar chaves de API e políticas do bucket
```

### Comandos de Debug

**Verificar logs do backend:**
```bash
# No dashboard do Render, vá para "Logs"
# Ou use a CLI do Render:
render logs -s recorder-backend
```

**Testar conectividade:**
```bash
# Testar health check
curl https://recorder-backend.onrender.com/actuator/health

# Testar API
curl https://recorder-backend.onrender.com/api/galeria
```

## ⚡ Otimizações de Performance

### Passo 18: Otimizar Dockerfile

```dockerfile
# Use cache de dependências Maven
COPY backend/pom.xml .
RUN mvn dependency:go-offline -B

# Use .dockerignore
echo "target/
.git/
*.md
.env" > .dockerignore
```

### Passo 19: Configurar Cache

**Backend:**
```properties
# Cache de recursos estáticos
spring.web.resources.cache.cachecontrol.max-age=31536000
spring.web.resources.cache.cachecontrol.cache-public=true

# Cache de respostas HTTP
spring.web.resources.cache.use-last-modified=true
```

**Frontend:**
```
# _headers file
/css/*
  Cache-Control: public, max-age=31536000

/js/*
  Cache-Control: public, max-age=31536000

/images/*
  Cache-Control: public, max-age=31536000
```

### Passo 20: Configurar CDN (Opcional)

Para melhor performance global:
1. Configure Cloudflare como proxy
2. Ative cache para recursos estáticos
3. Configure compressão Gzip/Brotli

## 📈 Escalabilidade

### Upgrade de Planos

**Quando considerar upgrade:**
- CPU usage > 80% consistentemente
- Memory usage > 90%
- Response time > 2 segundos
- Mais de 1000 usuários simultâneos

**Opções de upgrade:**
- **Starter**: $7/mês - 0.5 CPU, 512 MB RAM
- **Standard**: $25/mês - 1 CPU, 2 GB RAM
- **Pro**: $85/mês - 2 CPU, 4 GB RAM

### Auto-scaling

Configure auto-scaling baseado em:
- CPU utilization
- Memory usage
- Request rate
- Response time

## 🔒 Segurança

### Passo 21: Configurações de Segurança

**Variáveis de ambiente sensíveis:**
- Nunca commite senhas no código
- Use variáveis de ambiente do Render
- Rotacione chaves regularmente

**Headers de segurança:**
```properties
# application.properties
server.servlet.session.cookie.secure=true
server.servlet.session.cookie.http-only=true
server.servlet.session.cookie.same-site=strict
```

**Rate limiting:**
```java
@Component
public class RateLimitingFilter implements Filter {
    // Implementar rate limiting por IP
}
```

## 📞 Suporte e Recursos

- **Documentação Render**: [https://render.com/docs](https://render.com/docs)
- **Status Page**: [https://status.render.com](https://status.render.com)
- **Community Forum**: [https://community.render.com](https://community.render.com)
- **Support**: Disponível para planos pagos

## 🚀 Próximos Passos

Após o deploy:

1. Configure monitoramento avançado
2. Implemente CI/CD com GitHub Actions
3. Configure backup automático
4. Otimize performance baseado em métricas
5. Configure ambiente de staging

---

**Autor**: Manus AI  
**Data**: 2025  
**Versão**: 1.0

