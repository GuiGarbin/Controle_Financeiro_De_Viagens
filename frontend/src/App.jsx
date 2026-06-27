import { useState } from 'react'


import LoginPage from './pages/LoginPage'
import BootPage from './pages/BootPage'
import RegisterPage from './pages/RegisterPage'
import DashBoardPage from './pages/DashBoardPage'


function App() {

  const [currentPage, setCurrentPage] = useState('boot')

  function handleLoginSuccess() {
    setCurrentPage('dashboard')
  }


  return (
      <div>

        {currentPage === 'boot' && (
            // Ao escolher criar conta, vai para a tela de cadastro (antes ia direto para o dashboard)
            <BootPage onLogin={() => setCurrentPage('login')}
                      onRegister={() => setCurrentPage('register')}/>
        )}

        {currentPage === 'login' && (
            <LoginPage onLoginSuccess={handleLoginSuccess}
                      goToBoot={() => setCurrentPage('boot')}/>
        )}

        {/* Logout volta para a tela inicial (boot), onde dá pra entrar ou criar conta */}
        {currentPage === 'dashboard' && <DashBoardPage onLogout={() => setCurrentPage('boot')} />}

        {currentPage === 'register' && (
            <RegisterPage
                onRegisterSuccess={() => setCurrentPage('dashboard')}
                goToBoot={() => setCurrentPage('boot')}
            />
        )}
      </div>
  )
}

export default App