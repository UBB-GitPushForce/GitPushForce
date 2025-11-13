import apiClient from "./api-client";

interface LoginData {
    email: string;
    password: string;
}

interface RegisterData {
    first_name: string;
    last_name: string;
    phone_number: string;
    email: string;
    password: string;
}

interface AuthResponse {
    message: string;
    access_token: string;
    user: {
        id: number;
        first_name: string;
        last_name: string;
        email: string;
        phone_number: string;
        created_at: string;
        updated_at: string;
        hashed_password: string;
    };
}

interface UserResponse {
    id: number;
    first_name: string;
    last_name: string;
    email: string;
    phone_number: string;
}

class AuthService {
    login(credentials: LoginData) {
        return apiClient.post<AuthResponse>('/users/auth/login', credentials);
    }

    register(data: RegisterData) {
        return apiClient.post<AuthResponse>('/users/auth/register', data);
    }

    getMe() {
        return apiClient.get<UserResponse>('/users/auth/me');
    }

    logout() {
        return apiClient.post('/users/auth/logout');
    }
}

export default new AuthService();