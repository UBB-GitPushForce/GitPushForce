import apiClient from "./api-client";

interface Category {
    id: number;
    title: string;
    user_id: number;
    keywords?: string[];
}

interface APIResponse<T> {
    success: boolean;
    message?: string;
    data: T;
}

class CategoryService {
    async getCategories() {
        const response = await apiClient.get<APIResponse<Category[]>>('/categories');
        return response.data.data; // Extract data from APIResponse wrapper
    }

    async createCategory(title: string) {
        const response = await apiClient.post<APIResponse<{ id: number }>>('/categories', { 
            title,
            keywords: [] 
        });
        return response.data.data; // Extract data from APIResponse wrapper
    }
}

export default new CategoryService();
export type { Category };
