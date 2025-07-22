# Changelog - Migração MySQL para PostgreSQL

## [2.0.0] - 2025-01-21

### 🚀 Adicionado
- **Sistema de Upload Completo**: Implementação de upload de imagens e vídeos para Supabase Storage
- **Galeria Dinâmica**: Sistema de galeria com carregamento dinâmico da API
- **Entidade Galeria**: Nova entidade JPA para gerenciar mídias
- **Repository Galeria**: Interface JPA com métodos de busca customizados
- **Controller Galeria**: API REST completa para gerenciamento de mídias
- **Serviço Supabase**: Integração com Supabase Storage para upload/download
- **Modal de Visualização**: Modal responsivo para visualizar imagens e vídeos
- **Filtros por Tipo**: Filtros para separar fotos e vídeos na galeria
- **Configuração Docker**: Dockerfile e docker-compose para desenvolvimento
- **Documentação Completa**: Guias detalhados de configuração e deploy

### 🔄 Modificado
- **Banco de Dados**: Migração completa de MySQL para PostgreSQL
- **Driver JDBC**: Substituição do mysql-connector-j pelo postgresql
- **Dialect Hibernate**: Mudança para PostgreSQLDialect
- **URLs de Conexão**: Atualização para formato PostgreSQL
- **Frontend Upload**: Integração real com API em vez de simulação
- **Frontend Galeria**: Carregamento dinâmico em vez de dados estáticos
- **Configurações**: Adição de variáveis de ambiente para Supabase

### 🗃️ Arquivos Modificados

#### Backend
- `pom.xml`: Dependência MySQL → PostgreSQL
- `application.properties`: Configurações de banco e Supabase
- `persistence.xml`: Configurações JPA para PostgreSQL

#### Frontend
- `uploadGaleria.js`: Integração com API real
- `galeria.js`: Sistema dinâmico com modal e filtros

#### Novos Arquivos
- `Galeria.java`: Entidade JPA
- `GaleriaRepository.java`: Repository interface
- `GaleriaController.java`: REST Controller
- `SupabaseStorageService.java`: Serviço de upload
- `Dockerfile`: Configuração Docker
- `docker-compose.yml`: Orquestração de containers
- `.env.example`: Template de variáveis de ambiente

### 📚 Documentação
- `docs/supabase-setup.md`: Guia completo de configuração do Supabase
- `docs/render-deployment.md`: Guia de deploy no Render
- `docs/migration-guide.md`: Documentação detalhada da migração
- `README.md`: Documentação principal do projeto

### 🔧 Configurações
- **CORS**: Configuração para permitir acesso do frontend
- **Upload**: Limite de 10MB para arquivos
- **Security**: Headers de segurança e validações
- **Environment**: Suporte a variáveis de ambiente

### 🐛 Corrigido
- Problemas de compatibilidade entre MySQL e PostgreSQL
- Configurações de dialect incorretas
- URLs de conexão com parâmetros MySQL específicos
- Falta de integração real entre frontend e backend

### 🚨 Breaking Changes
- **Banco de Dados**: Necessária migração de dados do MySQL para PostgreSQL
- **Configurações**: Novas variáveis de ambiente obrigatórias
- **Deploy**: Nova estrutura de deploy com Docker

### 📋 Requisitos
- Java 17+
- PostgreSQL 12+
- Conta no Supabase
- Conta no Render (para deploy)

### 🔄 Migração
Para migrar de versão anterior:
1. Backup dos dados do MySQL
2. Configurar PostgreSQL local ou Supabase
3. Atualizar variáveis de ambiente
4. Executar aplicação (Hibernate criará tabelas automaticamente)
5. Migrar dados manualmente se necessário

### 🎯 Próximas Versões
- [ ] Cache Redis para performance
- [ ] Testes automatizados
- [ ] CI/CD pipeline
- [ ] Monitoramento APM
- [ ] Backup automático
- [ ] Thumbnails automáticos
- [ ] Compressão de imagens
- [ ] Search full-text

---

**Migração realizada por**: Manus AI  
**Data**: 21 de Janeiro de 2025  
**Versão**: 2.0.0

