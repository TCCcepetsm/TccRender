# Guia Completo de Configuração do Supabase

Este guia detalha como configurar o Supabase para hospedar o banco de dados PostgreSQL e o storage de arquivos para o projeto Recorder.

## 📋 Índice

1. [Criação da Conta e Projeto](#criação-da-conta-e-projeto)
2. [Configuração do Banco de Dados](#configuração-do-banco-de-dados)
3. [Configuração do Storage](#configuração-do-storage)
4. [Configuração das Variáveis de Ambiente](#configuração-das-variáveis-de-ambiente)
5. [Configuração de Segurança](#configuração-de-segurança)
6. [Testes de Conectividade](#testes-de-conectividade)
7. [Troubleshooting](#troubleshooting)

## 🚀 Criação da Conta e Projeto

### Passo 1: Criar Conta no Supabase

1. Acesse [https://supabase.com](https://supabase.com)
2. Clique em "Start your project"
3. Faça login com GitHub, Google ou crie uma conta com email
4. Confirme seu email se necessário

### Passo 2: Criar Novo Projeto

1. No dashboard, clique em "New Project"
2. Selecione sua organização (ou crie uma nova)
3. Preencha os dados do projeto:
   - **Name**: `recorder-app` (ou nome de sua preferência)
   - **Database Password**: Crie uma senha forte e **anote-a**
   - **Region**: Escolha a região mais próxima dos seus usuários
   - **Pricing Plan**: Selecione "Free" para começar

4. Clique em "Create new project"
5. Aguarde alguns minutos para o projeto ser criado

## 🗄️ Configuração do Banco de Dados

### Passo 3: Obter Credenciais do Banco

1. No dashboard do projeto, vá para **Settings** → **Database**
2. Na seção "Connection info", você encontrará:
   - **Host**: `db.xxx.supabase.co`
   - **Database name**: `postgres`
   - **Port**: `5432`
   - **User**: `postgres`
   - **Password**: A senha que você definiu na criação

### Passo 4: Configurar Connection String

A string de conexão será no formato:
```
postgresql://postgres:[SUA-SENHA]@db.[SEU-PROJETO].supabase.co:5432/postgres
```

### Passo 5: Configurar Tabelas (Opcional)

O Spring Boot criará as tabelas automaticamente com `hibernate.ddl-auto=update`, mas você pode criar manualmente se preferir:

1. Vá para **SQL Editor** no dashboard
2. Execute o seguinte SQL:

```sql
-- Criar tabela de galeria
CREATE TABLE IF NOT EXISTS galeria (
    id SERIAL PRIMARY KEY,
    midia_url VARCHAR(500) NOT NULL,
    tipo VARCHAR(50) NOT NULL CHECK (tipo IN ('FOTO', 'VIDEO')),
    profissional_id INTEGER,
    data_postagem TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_galeria_tipo ON galeria(tipo);
CREATE INDEX IF NOT EXISTS idx_galeria_profissional ON galeria(profissional_id);
CREATE INDEX IF NOT EXISTS idx_galeria_data ON galeria(data_postagem);
```

## 📁 Configuração do Storage

### Passo 6: Criar Bucket para Galeria

1. No dashboard, vá para **Storage**
2. Clique em "Create a new bucket"
3. Configure o bucket:
   - **Name**: `galeria`
   - **Public bucket**: ✅ Marque esta opção
   - **File size limit**: `10 MB` (ou conforme necessário)
   - **Allowed MIME types**: `image/*,video/*`

4. Clique em "Create bucket"

### Passo 7: Configurar Políticas de Acesso (RLS)

1. Ainda na seção Storage, clique no bucket `galeria`
2. Vá para a aba **Policies**
3. Clique em "Add policy" e configure:

**Política para Upload (INSERT):**
```sql
CREATE POLICY "Allow authenticated uploads" ON storage.objects
FOR INSERT WITH CHECK (
  bucket_id = 'galeria' AND
  auth.role() = 'authenticated'
);
```

**Política para Leitura Pública (SELECT):**
```sql
CREATE POLICY "Allow public downloads" ON storage.objects
FOR SELECT USING (bucket_id = 'galeria');
```

**Política para Deletar (DELETE):**
```sql
CREATE POLICY "Allow authenticated deletes" ON storage.objects
FOR DELETE USING (
  bucket_id = 'galeria' AND
  auth.role() = 'authenticated'
);
```

### Passo 8: Obter Chaves de API

1. Vá para **Settings** → **API**
2. Anote as seguintes informações:
   - **Project URL**: `https://[SEU-PROJETO].supabase.co`
   - **anon public**: Esta é sua chave pública
   - **service_role**: Esta é sua chave privada (use apenas no backend)

## 🔧 Configuração das Variáveis de Ambiente

### Passo 9: Configurar Backend

No arquivo `application.properties` do seu backend:

```properties
# Supabase Database Configuration
spring.datasource.url=postgresql://postgres:[SUA-SENHA]@db.[SEU-PROJETO].supabase.co:5432/postgres
spring.datasource.username=postgres
spring.datasource.password=[SUA-SENHA]

# Supabase Storage Configuration
supabase.url=https://[SEU-PROJETO].supabase.co
supabase.key=[SUA-CHAVE-ANON]
supabase.bucket=galeria
```

### Passo 10: Configurar Variáveis de Ambiente para Deploy

Para o deploy no Render, configure estas variáveis:

```bash
DATABASE_URL=postgresql://postgres:[SUA-SENHA]@db.[SEU-PROJETO].supabase.co:5432/postgres
SUPABASE_URL=https://[SEU-PROJETO].supabase.co
SUPABASE_ANON_KEY=[SUA-CHAVE-ANON]
SUPABASE_BUCKET=galeria
```

## 🔒 Configuração de Segurança

### Passo 11: Configurar Autenticação (Opcional)

Se você quiser usar a autenticação do Supabase:

1. Vá para **Authentication** → **Settings**
2. Configure os provedores desejados (Email, Google, GitHub, etc.)
3. Configure as URLs de redirecionamento
4. Ajuste as políticas de senha conforme necessário

### Passo 12: Configurar Row Level Security (RLS)

Para maior segurança, ative RLS nas tabelas:

```sql
-- Ativar RLS na tabela galeria
ALTER TABLE galeria ENABLE ROW LEVEL SECURITY;

-- Política para permitir leitura pública
CREATE POLICY "Allow public read" ON galeria
FOR SELECT USING (true);

-- Política para permitir inserção apenas para usuários autenticados
CREATE POLICY "Allow authenticated insert" ON galeria
FOR INSERT WITH CHECK (auth.role() = 'authenticated');

-- Política para permitir atualização apenas do próprio conteúdo
CREATE POLICY "Allow owner update" ON galeria
FOR UPDATE USING (auth.uid()::text = profissional_id::text);

-- Política para permitir deleção apenas do próprio conteúdo
CREATE POLICY "Allow owner delete" ON galeria
FOR DELETE USING (auth.uid()::text = profissional_id::text);
```

## 🧪 Testes de Conectividade

### Passo 13: Testar Conexão do Banco

Use um cliente PostgreSQL ou execute no SQL Editor:

```sql
SELECT version();
SELECT current_database();
SELECT current_user;
```

### Passo 14: Testar Upload de Arquivo

Teste o upload usando curl:

```bash
curl -X POST \
  'https://[SEU-PROJETO].supabase.co/storage/v1/object/galeria/test.txt' \
  -H 'Authorization: Bearer [SUA-CHAVE-ANON]' \
  -H 'Content-Type: text/plain' \
  -d 'Hello Supabase!'
```

### Passo 15: Testar Download Público

```bash
curl 'https://[SEU-PROJETO].supabase.co/storage/v1/object/public/galeria/test.txt'
```

## 🔧 Troubleshooting

### Problemas Comuns

**1. Erro de Conexão com Banco:**
- Verifique se a senha está correta
- Confirme se o host está correto
- Verifique se o firewall não está bloqueando a porta 5432

**2. Erro de Upload:**
- Verifique se o bucket existe e está público
- Confirme se as políticas de acesso estão configuradas
- Verifique se a chave de API está correta

**3. Erro de Permissão:**
- Verifique se RLS está configurado corretamente
- Confirme se o usuário tem as permissões necessárias
- Verifique se as políticas estão ativas

**4. Erro de CORS:**
- Configure CORS no Supabase em **Settings** → **API**
- Adicione seu domínio frontend na lista de origens permitidas

### Comandos Úteis para Debug

**Verificar tabelas:**
```sql
SELECT table_name FROM information_schema.tables 
WHERE table_schema = 'public';
```

**Verificar políticas RLS:**
```sql
SELECT schemaname, tablename, policyname, permissive, roles, cmd, qual 
FROM pg_policies WHERE tablename = 'galeria';
```

**Verificar buckets:**
```sql
SELECT * FROM storage.buckets;
```

**Verificar objetos no storage:**
```sql
SELECT * FROM storage.objects WHERE bucket_id = 'galeria';
```

## 📊 Monitoramento e Métricas

### Passo 16: Configurar Monitoramento

1. Vá para **Settings** → **Usage**
2. Monitore:
   - Database size
   - Storage usage
   - API requests
   - Bandwidth usage

### Limites do Plano Gratuito

- **Database**: 500 MB
- **Storage**: 1 GB
- **Bandwidth**: 2 GB
- **API requests**: 50,000/mês

## 🚀 Próximos Passos

Após configurar o Supabase:

1. Configure o deploy no Render seguindo o [guia de deploy](render-deployment.md)
2. Teste a aplicação completa
3. Configure backup automático se necessário
4. Monitore o uso e considere upgrade se necessário

## 📞 Suporte

- **Documentação oficial**: [https://supabase.com/docs](https://supabase.com/docs)
- **Discord da comunidade**: [https://discord.supabase.com](https://discord.supabase.com)
- **GitHub**: [https://github.com/supabase/supabase](https://github.com/supabase/supabase)

---

**Autor**: Manus AI  
**Data**: 2025  
**Versão**: 1.0

