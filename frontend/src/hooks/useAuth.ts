import {useEffect, useState} from 'react'
import {useLocation, useNavigate} from 'react-router-dom'
import authenticationService, {IncomingUser} from "../services/authenticationService";

function useAuth(redirectToSignIn?: boolean) {
	const [user, setUser] = useState<IncomingUser | null>(null)
	const {pathname} = useLocation()
	const navigate = useNavigate()

	useEffect(() => {
		authenticationService
			.getCurrentUser()
			.then(user => {
				setUser(user)
			})
			.catch((e) => {
				if (redirectToSignIn && e.response.status === 401) {
					window.sessionStorage.setItem('signInRedirect', pathname || '/')
					navigate('/login')
				}
			})
	}, [pathname, navigate, redirectToSignIn])

	return user
}

export default useAuth
