import { apiClient } from "./client";

export interface UserResponse {
    userId: string;
    email: string;
    username: string;
}

export async function updateUsername(username: string): Promise<UserResponse> {
    const { data } = await apiClient.put<UserResponse>("/api/users/username", { username });
    return data;
}

export async function deleteAccount(): Promise<void> {
    await apiClient.delete("/api/users/me");
}