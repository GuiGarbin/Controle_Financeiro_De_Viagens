import { useState } from 'react'


import AuthFlow from './pages/AuthFlow'
import DashBoardPage from './pages/DashBoardPage'
import CreateTripPage from './pages/CreateTripPage'
import AddExpensePage from './pages/AddExpensePage'
import TitleBar from './components/TitleBar'
import appStyles from './App.module.css'


function App() {

  const [currentPage, setCurrentPage] = useState('auth')
  // Id do usuário logado (retornado por login/cadastro). Usado para escopar as
  // viagens por dono (header X-User-Id). null = ninguém logado.
  const [userId, setUserId] = useState(null)

  function handleAuthSuccess(id) {
    setUserId(id)
    setCurrentPage('dashboard')
  }

  function handleLogout() {
    setUserId(null)
    setCurrentPage('auth')
  }


  return (
      <div className={appStyles.frame}>
        <TitleBar />
        <div className={appStyles.content}>

        {currentPage === 'auth' && (
            <AuthFlow onAuthSuccess={handleAuthSuccess} />
        )}

        {/* Logout volta para a tela de autenticação */}
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
        </div>
      </div>
  )
}

export default App