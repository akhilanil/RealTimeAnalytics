// TopPages.tsx
import {
  Card, CardContent, CardDescription, CardHeader, CardTitle,
} from "@/components/ui/card";
import { type PageView } from "@/model/PageView";
import { Bar, BarChart, CartesianGrid, XAxis } from "recharts";
import {
  type ChartConfig,
  ChartContainer,
  ChartLegend,
  ChartLegendContent,
  ChartTooltip,
  ChartTooltipContent,
} from "@/components/ui/chart";
import { Skeleton } from "@/components/ui/skeleton";


const chartConfig = {
  pageUrl: { label: "PageUrl", color: "#2563eb" },
} satisfies ChartConfig;

type TopPagesProps = {
  data: PageView[];
  title?: string;
  loading?: boolean;
};

const TopPages = ({ data, title = "Top Pages", loading = false }: TopPagesProps) => {
  return (
    // <Card className="w-full max-w-sm">
    <Card className="w-full max-w-sm">
      <CardHeader>
        <CardTitle>
          {loading ? <Skeleton className="h-6 w-40" /> : title}
        </CardTitle>
        <CardDescription>
          {loading ? (
            <div className="space-y-3">
              {/* chart skeleton */}
              <Skeleton className="h-[220px] w-full" />
              {/* legend/labels skeleton */}
              <div className="flex items-center gap-2">
                <Skeleton className="h-4 w-20" />
                <Skeleton className="h-4 w-16" />
              </div>
            </div>
          ) : (
            <ChartContainer config={chartConfig} className="min-h-[200px] w-full">
              <BarChart accessibilityLayer data={data}>
                <CartesianGrid vertical={false} />
                <XAxis
                  dataKey="pageUrl"
                  tickLine={false}
                  tickMargin={10}
                  axisLine={false}
                  tickFormatter={(value: string) => value.slice(0, 3)}
                />
                <ChartTooltip content={<ChartTooltipContent />} />
                <ChartLegend content={<ChartLegendContent />} />
                <Bar dataKey="count" fill="var(--color-pageUrl)" radius={4} />
              </BarChart>
            </ChartContainer>
          )}
        </CardDescription>
      </CardHeader>
      <CardContent>
        {!loading && data.length === 0 ? (
          <div className="text-sm text-muted-foreground">No data available.</div>
        ) : null}
      </CardContent>
    </Card>
  );
};

export default TopPages;
