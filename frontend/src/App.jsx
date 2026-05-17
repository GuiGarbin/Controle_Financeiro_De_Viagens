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
            <BootPage onLogin={() => setCurrentPage('login')}
                      onRegister={() => setCurrentPage('dashboard')}/>
        )}

        {currentPage === 'login' && (
            <LoginPage onLoginSuccess={handleLoginSuccess}
                      goToBoot={() => setCurrentPage('boot')}/>
        )}

        {currentPage === 'dashboard' && <DashBoardPage />}

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