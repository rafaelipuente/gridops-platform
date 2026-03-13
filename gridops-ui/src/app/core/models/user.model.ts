export interface LoginRequest {
  username: string;
  password: string;
}

export interface LoginResponse {
  token: string;
  username: string;
  role: string;
}

export interface UserSummary {
  id: number;
  username: string;
  role: string;
}
