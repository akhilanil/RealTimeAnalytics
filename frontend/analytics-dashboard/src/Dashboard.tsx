import ActiveUsersCountComponent from "./components/ActiveUsersCountComponent";
import ActiveSessionsCountComponent from "./components/ActiveSessionsCountComponent";
import ActiveSessionsComponent from "./components/ActiveSessionsComponent";

import TopPages from "./components/TopPages";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";

import { useEffect, useState } from "react";
import { type PageView } from "@/model/PageView";
import { type UserDetails } from "@/model/UserDetails";
import { getPageViews, getUserDetails } from "@/services/DashboardApi";




const Dashboard = () => {

  const [pageViews, setPageViews] = useState<PageView[]>([]);
  const [userDetails, setUserDetails] = useState<UserDetails[]>([]);
  const [userCount, setuserCount] = useState<number>(0);
  const [sessionCount, setSessionCount] = useState<number>(0);
  
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchData = async () => {
      try {
        const { pageViews } = await getPageViews();
        setPageViews(pageViews);
  
        const { userDetails } = await getUserDetails();
        setUserDetails(userDetails);
        setuserCount(userDetails.length);
        setSessionCount(
          userDetails.reduce((total, user) => total + user.sessionCount, 0)
        );
      } finally {
        setLoading(false);
      }
    };
  
    // fetch immediately
    fetchData();
  
    // set interval for 30 seconds
    const intervalId = setInterval(fetchData, 30000);
  
    // cleanup
    return () => clearInterval(intervalId);
  }, []);

  return (
    <Card className="w-full">
      <CardHeader>
        <CardTitle>LiftLab Analytics</CardTitle>
      </CardHeader>
      <CardContent>
        <div className="flex gap-4 w-full">
            <ActiveUsersCountComponent userCount={userCount}></ActiveUsersCountComponent>
            <ActiveSessionsCountComponent sessionCount={sessionCount}></ActiveSessionsCountComponent>
        </div>
        <div className="flex gap-4 w-full">
        <TopPages
            data={pageViews}
            title="Top Pages"
            loading={loading}   // spreads to share row space
          />
          <ActiveSessionsComponent data={userDetails} loading={loading} className="max-w-sm" />
        </div>
        
      </CardContent>
    </Card>
  );
};

export default Dashboard;
