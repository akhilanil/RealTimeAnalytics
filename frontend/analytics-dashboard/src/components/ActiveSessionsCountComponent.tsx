import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
// import { Label } from "@/components/ui/label"

type ActiveSessionData = {
  sessionCount: number
};

const ActiveSessionsCountComponent = ({sessionCount}: ActiveSessionData) => {
  return (
    <Card className="w-full max-w-sm">
      <CardHeader>
        <CardTitle>{sessionCount}</CardTitle>
        <CardDescription>
        Active Sessions in last 5 minutes
        </CardDescription>
      </CardHeader>
      <CardContent></CardContent>
    </Card>
  );
};

export default ActiveSessionsCountComponent;
