import React, {createContext, useState, useCallback, ReactNode, useEffect} from 'react';
import authService from '../services/auth-service';

interface User {
    id: number;
    first_name: string;
    last_name: string;
    email: string;
    phone_number: string;
}

interface AuthContextType {
    user: User | null;
    // token: string | null;
    login: (email: string, password: string) => Promise<void>;
    register: (userData: any) => Promise<void>;
    logout: () => void;
    isAuthenticated: boolean;
    isChecking: boolean;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<User | null>(null);
    // const [token, setToken] = useState<string | null>(() => {
    //     return sessionStorage.getItem('access_token');
    // });
    const [isChecking, setIsChecking] = useState(true);

    useEffect(() => {
        checkAuthStatus();
    }, []);

    const checkAuthStatus = async () => {
        try {
            const response = await authService.getMe(); // GET /auth/me
            setUser(response.data);
        } catch {
            setUser(null);
        } finally {
            setIsChecking(false);
        }
    };

    const login = useCallback(async (email: string, password: string) => {
        const response = await authService.login({ email, password });
        setUser(response.data.user);
    }, []);



    const register = useCallback(async (accountData: any) => {
        const response = await authService.register(accountData);
        setUser(response.data.user);

        // const { access_token, user } = response.data;
        // setToken(access_token);
        // setUser(user);
        // sessionStorage.setItem('access_token', access_token);
    }, []);

    const logout = useCallback(async () => {
        try {
            await authService.logout();
            setUser(null);
        } catch (error) {
            console.error('Logout error:', error);
            setUser(null);
        }
    }, []);


    const value: AuthContextType = {
        user,
        login,
        register,
        logout,
        isAuthenticated: !!user,
        isChecking,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );

};
