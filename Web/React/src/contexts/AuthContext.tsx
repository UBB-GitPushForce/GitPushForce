import React, {createContext, useState, useCallback, ReactNode, useEffect} from 'react';
import authService from '../services/auth-services';
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
            setUser(response.data);
        } catch {
            setUser(null);
        } finally {
            setIsChecking(false);
        }
    };

    const login = useCallback(async (email: string, password: string) => {
        // call login endpoint
        const response = await authService.login({ email, password });
        
        // If backend indicates 2FA is required, it should return e.g.:
        // { two_fa_required: true, two_fa_method: 'email', temp_token: '...' }
        // For now, we support that shape if backend provides it.
        if ((response as any).data?.two_fa_required) {
            const method = (response as any).data?.two_fa_method || 'email';
            const tempToken = (response as any).data?.temp_token || null;
            setPendingTwoFactor({ method, tempToken });
            // DO NOT set user yet — wait for verifyTwoFactor
            return;
        }

        // If backend doesn't return that shape, proceed as usual:
        // NOTE: some backends can return user directly
        if ((response as any).data?.user) {
            setUser((response as any).data.user);
        } else {
            // fallback: attempt to fetch /me
            try {
                const me = await authService.getMe();
                setUser(me.data);
            } catch (err) {
                // keep user null
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
                // If backend supports getMe, use it. Here we try to fetch user after successful verify.
                const me = await authService.getMe();
                setUser(me.data);
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
        // TODO: register endpoint should kick off verification (send code to email/sms)
        // e.g. const res = await authService.register({ ...accountData, verification_method: verificationMethod });
        // For now call register and then attempt to fetch /me or instruct UI to verify code.
        const response = await authService.register(accountData);
        // Some backends may not authenticate immediately; we'll try to fetch /me
        try {
            const me = await authService.getMe();
            setUser(me.data);
        } catch (err) {
            // registration may require verification — keep user null and leave registration flow to component
            console.warn('Register: user not automatically authenticated (verification may be required).', err);
        }
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
        pendingTwoFactor,
        verifyTwoFactor,
        cancelTwoFactor,
    };

    return (
        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>
    );
};

