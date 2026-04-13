# Controle de Viagens
 Descrição do sistema: sistema focado no controle de gastos durante uma viagem, realizando: criação de viagem, 
 registro de custo de pontos turísticos, registro de gastos,
 conversão de moeda, calculo de limite de gastos diário e total.
 
 Objetivo: Tornar mais fácil o controle financeiro durante uma viagem, focando no desenvolvimento de um software
 rápido e fácil de realizar as anotações de gastos e visualização fácil dos valores durante uma viagem.

 Projeto Electron utilizando React para o Frontend e Java SpringBoot para o backend.
 
 Lista inicial de funcionalidades: Criação de conta e salvamento de dados
 Registro de orçamento
 Administrar um destino por viagem
 Registro de custo de pontos turísticos
 Registros de gastos durante a viagem
 Conversão dos valores de moeda local
 Averiguação de limite de gostos e aviso ao usuário
 Operações de registro, cálculo e conversão offline

 # Instalação
 1) **Certifique-se que você tem o [Java 21](https://jdk.java.net/java-se-ri/21) e o [Maven](https://maven.apache.org/download.cgi) instalados**
 2) **Clone o repositório com**
  ```bash
  Git clone https://github.com/GuiGarbin/Controle_Financeiro_De_Viagens
  ```
  3) **Entre na pasta \frontend\ e rode o comando** 
  ```bash
  npm install
  ```
<p>Isso irá instalar as dependências do node necessárias</p>

A partir daqui temos três opções para executar o sistema:
 - Rodar o backend e o front de forma separada, afim de testar cada um de forma independente
 - Rodar o backend e o front em conjunto, lançando o aplicativo Electron
 - Empacotar o aplicativo em um instalador e rodar um executável
 ## Testar Backend e Frontend separadamente:
 ### Iniciar backend
 1) Navegue até a pasta \backend\ e rode nela o comando
 ```bash
 mvn spring-boot:run
 ``` 
 <p>Isso irá iniciar o servidor backend e, a partir daqui, você pode abrir um outro terminal e testar um curl em http://localhost:8080/api/(endpoint_desejado)</p>
 
 ### Iniciar frontend
 1) Navegue até a pasta \frontend\ e rode nela o comando
 ```bash
 npm run dev:vite
 ``` 
 <p>Isso irá iniciar a interface frontend em http://localhost:5173/. Acesse esse endereço no navegador e você deverá ver a tela inicial do sistema. Caso o backend não esteja rodando, ao tentar executar o login (ou demais funções que procurem o backend) você vai ser deparar com o erro "Failed to Fetch".</p>
 
 ## Testar frontend e backend em conjunto (lançar o aplicativo Electron)
 <p>Essa opção vai abrir tanto o back quanto o front ao mesmo tempo no ambiente de desenvolvimento.</p>

1) Navegue até a pasta \frontend\ e rode o comando
```bash
npm run dev
```
Isso irá iniciar ao mesmo tempo o backend e o frontend, e irá abrir a janela do aplicativo. Aqui você pode testar como se fosse um usuário.

## Empacotar aplicativo em exe
<p>Entre na pasta \frontend\ e rode o comando

```bash
npm run build:exe
``` 

<p>Essa opção deve ser usada para empacotar o aplicativo para produção.</p><p>Após a compilação, será gerado um instalador na pasta \frontend\release, que pode ser executado para fazer a instalação do aplicativo.</p><p> Note que após a build, mudanças de código não surtirão efeito imediato: será necessário compilar a instalar o exe novamente.
