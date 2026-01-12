// src/services/category-service.ts
import apiClient from "./api-client";

export interface Category {
    id: number;
    title: string;
    user_id: number;
    keywords?: string[];
}

// Flexible response type handles both wrapped and unwrapped data
type APIResponseOrList<T> = T | { success: boolean; data: T };

class CategoryService {
    async getCategories(userId?: number) {
        // If userId is provided, fetch user-specific categories
        // Otherwise fetch all categories (requires auth but returns all)
        const endpoint = userId ? `/categories/${userId}` : '/categories/';
        
        const response = await apiClient.get<APIResponseOrList<Category[]>>(endpoint);
        
        console.log("Categories API Response:", response.data); // Debugging log

        // 1. If response.data is directly an array
        if (Array.isArray(response.data)) {
            return response.data;
        }
        // 2. If response.data is an object with a 'data' array
        if (response.data && 'data' in response.data && Array.isArray((response.data as any).data)) {
            return (response.data as any).data;
        }
        
        return [];
    }

    // UPDATE: Added keywords argument here
    async createCategory(title: string, keywords: string[] = []) {
        // CHANGED: Added trailing slash '/categories/'
        const response = await apiClient.post<APIResponseOrList<Category>>('/categories/', { 
            title,
            keywords: keywords // Pass the keywords array to the backend
        });

        const raw = response.data;
        if (raw && 'data' in (raw as any)) return (raw as any).data;
        return raw; 
    }

    async updateCategory(id: number, title: string, keywords: string[] = []) {
        const response = await apiClient.put(`/categories/${id}`, { 
            title, 
            keywords 
        });
        return response.data;
    }

    async deleteCategory(id: number) {
        // CHANGED: Added trailing slash '/categories/{id}/' (optional but good practice)
        await apiClient.delete(`/categories/${id}`);
        return true;
    }
}

export default new CategoryService();