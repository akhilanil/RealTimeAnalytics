import { type PageViewsResponse } from "@/model/PageView";
import { type UserDetailsResponse } from "@/model/UserDetails";



export const getPageViews = async (): Promise<PageViewsResponse> => {

  const url = '/api/v1/dashboard/page-views'

  const response = await fetch(url);
  if (!response.ok) {
    throw new Error(`Failed to fetch page views: ${response.status} ${response.statusText}`);
  }

  return response.json() as Promise<PageViewsResponse>;
};
  
 
  
export const getUserDetails = async (): Promise<UserDetailsResponse> => {

  const url = '/api/v1/dashboard/active-users'

  const response = await fetch(url);

  if (!response.ok) {
    throw new Error(`Failed to fetch page views: ${response.status} ${response.statusText}`);
  }

  return response.json() as Promise<UserDetailsResponse>;

}



