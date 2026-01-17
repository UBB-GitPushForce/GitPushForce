import React, {createContext, useState, useCallback, ReactNode, useEffect} from 'react';
import authService from '../services/auth-service';
import apiClient from '../services/api-client';

interface User {
    id: number;
    first_name: string;
    last_name: string;
    email: string;
    phone_number: string;
}

type TwoFAPending = {
    method: 'email'|'number'|'app'|null;
    tempToken?: string | null; // returned by backend to correlate verification
} | null;

interface AuthContextType {
    user: User | null;
    login: (email: string, password: string) => Promise<void>;
    register: (userData: any, verificationMethod?: 'email'|'number') => Promise<void>;
    logout: () => void;
    isAuthenticated: boolean;
    isChecking: boolean;

    // two-factor related
    pendingTwoFactor: TwoFAPending;
    verifyTwoFactor: (code: string) => Promise<void>;
    cancelTwoFactor: () => void;
    refreshUser: () => Promise<void>;
    updateUser: (patch: Partial<User>) => void;
}

export const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: ReactNode }) => {
    const [user, setUser] = useState<User | null>(null);
    const [isChecking, setIsChecking] = useState(true);

    // two-factor pending state
    const [pendingTwoFactor, setPendingTwoFactor] = useState<TwoFAPending>(null);

    useEffect(() => {
        checkAuthStatus();
    }, []);

    const checkAuthStatus = async () => {
        try {
            const response = await authService.getMe(); // GET /users/auth/me
            // Backend returns UserResponse - check if wrapped
            const userData = response.data?.data || response.data;
            setUser(userData);
        } catch {
            setUser(null);
        } finally {
            setIsChecking(false);
        }
    };

    const login = useCallback(async (email: string, password: string) => {
        // call login endpoint
        const response = await authService.login({ email, password });
        
        console.log('Login response:', response.data);
        
        // Backend returns AuthResponse - check if wrapped in data object
        const responseData = response.data?.data || response.data;
        
        // If backend indicates 2FA is required, it should return e.g.:
        // { two_fa_required: true, two_fa_method: 'email', temp_token: '...' }
        if (responseData?.two_fa_required) {
            const method = responseData?.two_fa_method || 'email';
            const tempToken = responseData?.temp_token || null;
            setPendingTwoFactor({ method, tempToken });
            // DO NOT set user yet — wait for verifyTwoFactor
            return;
        }

        // Store access token in sessionStorage
        if (responseData?.access_token) {
            sessionStorage.setItem('access_token', responseData.access_token);
        }

        // Extract user from response
        if (responseData?.user) {
            setUser(responseData.user);
        } else {
            // fallback: attempt to fetch /me
            try {
                const me = await authService.getMe();
                const userData = me.data;
                setUser(userData);
            } catch (err) {
                console.error('Login: failed to fetch /me after login', err);
            }
        }
    }, []);

    const verifyTwoFactor = useCallback(async (code: string) => {
        if (!pendingTwoFactor) throw new Error('No two-factor challenge pending');

        // TODO: call backend verify endpoint, e.g.:
        // const res = await apiClient.post('/users/auth/verify-2fa', { temp_token: pendingTwoFactor.tempToken, code });
        // then setUser(res.data.user) or call getMe().

        // MOCK behavior: accept code '123456' as valid
        if (code === '123456') {
            try {
                const me = await authService.getMe();
                const userData = me.data?.data || me.data;
                setUser(userData);
            } catch (err) {
                // If /me fails, try to set a minimal mock user (DEV fallback)
                setUser({
                    id: 9999,
                    first_name: 'Mock',
                    last_name: 'User',
                    email: 'mock@example.com',
                    phone_number: '',
                });
            } finally {
                setPendingTwoFactor(null);
            }
        } else {
            throw new Error('Invalid verification code (mock)');
        }
    }, [pendingTwoFactor]);

    const cancelTwoFactor = useCallback(() => {
        setPendingTwoFactor(null);
    }, []);

    const register = useCallback(async (accountData: any, verificationMethod?: 'email'|'number') => {
        // Register endpoint returns AuthResponse { access_token, user }
        const response = await authService.register(accountData);
        const responseData = response.data?.data || response.data;
        
        // Store access token in sessionStorage
        if (responseData?.access_token) {
            sessionStorage.setItem('access_token', responseData.access_token);
        }
        
        // Extract user from register response
        if (responseData?.user) {
            setUser(responseData.user);
        } else {
            // fallback: attempt to fetch /me
            try {
                const me = await authService.getMe();
                const userData = me.data?.data || me.data;
                setUser(userData);
            } catch (err) {
                console.warn('Register: user not automatically authenticated (verification may be required).', err);
            }
        }
    }, []);

    const logout = useCallback(async () => {
        try {
            await authService.logout();
            sessionStorage.removeItem('access_token');
            setUser(null);
        } catch (error) {
            console.error('Logout error:', error);
            sessionStorage.removeItem('access_token');
            setUser(null);
        }
    }, []);

    const refreshUser = useCallback(async () => {
        try {
            const response = await authService.getMe();
            const userData = response.data?.data || response.data;
            setUser(userData);
        } catch (err) {
            console.warn('refreshUser failed', err);
        }
    }, []);

    const updateUser = useCallback((patch: Partial<User>) => {
        setUser(prev => prev ? ({ ...prev, ...patch }) : prev);
    }, []);

    const value: AuthContextType = {
        user,
        login,
        register,
        logout,
        isAuthenticated: !!user,
        isChecking,
        pendingTwoFactor,
        verifyTwoFactor,
        cancelTwoFactor,
        refreshUser,
        updateUser,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

