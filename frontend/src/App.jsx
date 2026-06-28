import { useState } from 'react'


import LoginPage from './pages/LoginPage'
import BootPage from './pages/BootPage'
import RegisterPage from './pages/RegisterPage'
import DashBoardPage from './pages/DashBoardPage'
import CreateTripPage from './pages/CreateTripPage'
import AddExpensePage from './pages/AddExpensePage'
import TitleBar from './components/TitleBar'
import appStyles from './App.module.css'


function App() {

  const [currentPage, setCurrentPage] = useState('boot')
  // Id do usuário logado (retornado por login/cadastro). Usado para escopar as
  // viagens por dono (header X-User-Id). null = ninguém logado.
  const [userId, setUserId] = useState(null)

  function handleAuthSuccess(id) {
    setUserId(id)
    setCurrentPage('dashboard')
  }

  function handleLogout() {
    setUserId(null)
    setCurrentPage('boot')
  }


  return (
      <div className={appStyles.frame}>
        <TitleBar />
        <div className={appStyles.content}>

        {currentPage === 'boot' && (
            // Ao escolher criar conta, vai para a tela de cadastro (antes ia direto para o dashboard)
            <BootPage onLogin={() => setCurrentPage('login')}
                      onRegister={() => setCurrentPage('register')}/>
        )}

        {currentPage === 'login' && (
            <LoginPage onLoginSuccess={handleAuthSuccess}
                      goToBoot={() => setCurrentPage('boot')}/>
        )}

        {/* Logout volta para a tela inicial (boot), onde dá pra entrar ou criar conta */}
        {currentPage === 'dashboard' && (
            <DashBoardPage
                userId={userId}
                onLogout={handleLogout}
                onCreateTrip={() => setCurrentPage('createTrip')}
                onAddExpense={() => setCurrentPage('addExpense')}
            />
        )}

        {/* Voltar ao dashboard remonta o DashBoardPage, que rebusca a viagem atual */}
        {currentPage === 'createTrip' && (
            <CreateTripPage
                userId={userId}
                onCreated={() => setCurrentPage('dashboard')}
                goToDashboard={() => setCurrentPage('dashboard')}
            />
        )}

        {currentPage === 'addExpense' && (
            <AddExpensePage
                userId={userId}
                onAdded={() => setCurrentPage('dashboard')}
                goToDashboard={() => setCurrentPage('dashboard')}
            />
        )}

        {currentPage === 'register' && (
            <RegisterPage
                onRegisterSuccess={handleAuthSuccess}
                goToBoot={() => setCurrentPage('boot')}
            />
        )}
        </div>
      </div>
  )
}

export default App