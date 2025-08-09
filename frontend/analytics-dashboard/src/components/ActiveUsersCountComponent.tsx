import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
// import { Label } from "@/components/ui/label"

type ActiveUserData = {
  userCount: number
};


const ActiveUsersCountComponent = ({userCount}: ActiveUserData) => {
  return (
    <Card className="w-full max-w-sm">
      <CardHeader>
        <CardTitle>{userCount}</CardTitle>
        <CardDescription>
        Active Users in last 5 minutes
        </CardDescription>
      </CardHeader>
      <CardContent></CardContent>
    </Card>
  );
};

export default ActiveUsersCountComponent;
