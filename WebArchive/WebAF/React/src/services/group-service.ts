// src/services/group-service.ts
import apiClient from './api-client';

export interface Group {
  id: number;
  name: string;
  description?: string;
  created_at?: string;
  invitation_code?: string;
}

class GroupService {
  async getUserGroups(userId: number): Promise<Group[]> {
    const response = await apiClient.get(`/groups/user/${userId}`);
    return response.data.data || response.data;
  }

  async getGroupById(groupId: number): Promise<Group> {
    const response = await apiClient.get(`/groups/${groupId}`);
    return response.data.data || response.data;
  }
}

export default new GroupService();