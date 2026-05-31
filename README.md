# JOIA 2026 - Jogos Acadêmicos Universidários 🏆

O **JOIA 2026** é um aplicativo Android desenvolvido para centralizar e facilitar o acompanhamento dos jogos acadêmicos da universidade. Com ele, alunos e organizadores podem acompanhar placares em tempo real, conferir calendários, visualizar rankings e gerenciar informações dos jogos.

## 🚀 Funcionalidades

### Para Usuários (Viewers)
- **Feed de Jogos:** Lista completa de jogos com filtros por status: Ao Vivo, Agendado e Finalizado.
- **Detalhes da Partida:** Informações completas sobre cada jogo, incluindo súmula (cartões), escalações e local.
- **Sistema de Favoritos:** Salve os jogos de maior interesse para acesso rápido no perfil (sincronizado com o servidor).
- **Rankings:** Acompanhe a pontuação geral do "Troféu Rotativo" e o desempenho por modalidade.
- **Perfil Personalizado:** Gerenciamento de dados cadastrais e foto de perfil.
- **Cursos e Modalidades:** Consulta de informações técnicas e equipes participantes.

### Para Organizadores (Admins)
- **Painel Administrativo:** Acesso restrito para criação e gerenciamento de equipes.
- **Atualização de Placar:** Ferramenta para atualizar os resultados dos jogos em tempo real.

## 🛠 Tecnologias Utilizadas

- **Linguagem:** [Kotlin](https://kotlinlang.org/)
- **Arquitetura:** MVVM (Model-View-ViewModel) adaptada com Repositories.
- **Consumo de API:** [Retrofit 2](https://square.github.io/retrofit/) & OkHttp.
- **Serialização de Dados:** GSON.
- **Assincronismo:** Kotlin Coroutines & Flow.
- **Interface:** Material Design Components (Material 3).
- **Persistência Local:** SharedPreferences para gerenciamento de sessão e tokens.

## 📦 Estrutura do Projeto

- `JoiaRepository`: Ponto central de acesso aos dados (API).
- `UserSession`: Gerenciamento seguro do token JWT e dados do usuário logado.
- `JoiaApiService`: Definição dos endpoints REST do servidor.
- `JogosFragment` & `FavoritosFragment`: Exibição das listas dinâmicas de partidas.
- `JogoDetalheActivity`: Página rica em detalhes construída dinamicamente.

## ⚙️ Configuração e Instalação

1. Clone o repositório:
   ```bash
   git clone https://github.com/seu-usuario/Joia2026.git
   ```
2. Abra o projeto no **Android Studio**.
3. Certifique-se de que o `BASE_URL` no `RetrofitClient` aponta para o servidor ativo do professor.
4. Compile e execute em um dispositivo físico ou emulador (API 26+).

## 📄 Licença

Este projeto foi desenvolvido para fins acadêmicos. Todos os direitos reservados aos desenvolvedores e à universidade.

---
*Desenvolvido com ❤️ para o JOIA 2026.*
