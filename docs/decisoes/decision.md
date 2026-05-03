Inicialmente o planejado era uma trip ter uma list de expensives>
porem foi encontrada uma dificuldade em lidar com os expensives
de um dia especifico
Solucao: foi decidido criar uma camada intermediaria entre trip e expensive,
a classe dailybudget, aonde uma trip possui uma list de dailyBudget > e cada
obj dailybudget possui uma list de expensives>

Foi decidido NAO MEXER variavel budget, o calculo sera feito calculando
a lista de expenses

A classe expenseController deixa de concentrar funcoes de conversao e calculo
de orcamento restante e as contas passam a ser feitas pelos proprios objetos
